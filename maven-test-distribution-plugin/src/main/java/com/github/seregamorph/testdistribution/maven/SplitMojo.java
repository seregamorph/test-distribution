package com.github.seregamorph.testdistribution.maven;

import com.github.seregamorph.testdistribution.DistributionProvider;
import com.github.seregamorph.testdistribution.SimpleDistributionProvider;
import com.github.seregamorph.testdistribution.TestDistributionParameters;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.util.DirectoryScanner;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.surefire.api.testset.TestListResolver;
import org.apache.maven.surefire.api.util.DefaultScanResult;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This goal splits module test classes according to chosen {@link DistributionProvider} and writes the distribution
 * to the JSON file in the build directory.
 *
 * @author Sergey Chernov
 */
@Mojo(
        name = "split",
        requiresDependencyResolution = ResolutionScope.TEST,
        defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES,
        threadSafe = true)
public class SplitMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}")
    private File buildDir;

    @Parameter(defaultValue = "${project.build.outputDirectory}")
    private File classesDirectory;

    @Parameter(defaultValue = "${project.build.testOutputDirectory}")
    private File testClassesDirectory;

    @Parameter(property = "testDistribution.split.skip")
    private boolean skip = false;

    @Parameter(property = "testDistribution.includes")
    private List<String> includes;

    @Parameter(property = "testDistribution.excludes")
    private List<String> excludes;

    @Parameter(property = "testDistribution.initialSort")
    private boolean initialSort = true;

    @Parameter(property = "testDistribution.distributionProvider")
    private String distributionProvider = SimpleDistributionProvider.class.getName();

    @Parameter(property = "testDistribution.testGroupName")
    private String testGroupName;

    @Parameter(property = "testDistribution.numGroups")
    private int numGroups;

    @Parameter(property = "testDistribution.minGroupSize")
    private int minGroupSize = 1;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Skipping test classes distribution");
            return;
        }

        if (!"jar".equals(project.getPackaging())) {
            getLog().info("Test distribution split is only supported for jar packaging");
            return;
        }

        if (!buildDir.exists()) {
            getLog().warn("Build directory " + buildDir + " does not exist, skipping test classes distribution");
            return;
        }

        if (testGroupName == null || testGroupName.isEmpty()) {
            throw new MojoExecutionException("Plugin configuration should declare `testGroupName` parameter");
        }

        if (includes == null || includes.isEmpty()) {
            throw new MojoExecutionException("Plugin configuration should declare `includes` parameter with class name filtering");
        }

        if (numGroups <= 0) {
            throw new MojoExecutionException("Plugin configuration should declare `numGroups` parameter with value greater than 0");
        }

        getLog().info("Distributing test classes from " + testClassesDirectory + ", includes " + includes
                + (excludes.isEmpty() ? "" : ", excludes " + excludes) + " to " + numGroups + " groups");

        DefaultScanResult scanResult = scanTestClassesDirectory();

        Collection<URL> urls = getClasspathUrls();
        List<String> testClassNames = new ArrayList<>(scanResult.getClasses());
        if (testClassNames.isEmpty()) {
            getLog().info("No matching test classes found in " + testClassesDirectory);
        }
        if (initialSort) {
            Collections.sort(testClassNames);
        }
        TestDistributionParameters parameters = new TestDistributionParameters(numGroups, getModuleName(), minGroupSize);
        List<List<String>> testClassesGroups = splitTestClasses(urls, testClassNames, parameters);
        List<TestGroupEntity> testGroups = new ArrayList<>();
        for (int i = 0; i < testClassesGroups.size(); i++) {
            List<String> testClasses = testClassesGroups.get(i);
            String groupName = testGroupName + "-" + (i + 1);
            if (!testClasses.isEmpty()) {
                getLog().info("Test group " + groupName + ": " + testClasses);
            }
            testGroups.add(new TestGroupEntity()
                    .setName(groupName)
                    .setTestClasses(testClasses));
        }
        TestDistributionEntity entity = new TestDistributionEntity()
                .setModuleName(getModuleName())
                .setDistributionProvider(distributionProvider)
                .setNumGroups(numGroups)
                .setGroups(testGroups);

        File testDistributionFile = new File(buildDir, "test-distribution-" + testGroupName + ".json");
        JsonUtils.writeEntity(testDistributionFile, entity);
    }

    private List<List<String>> splitTestClasses(Collection<URL> urls, List<String> testClasses, TestDistributionParameters parameters) throws MojoExecutionException {
        // todo support fork option
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader pluginClassLoader = SplitMojo.class.getClassLoader();
        try (URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]), pluginClassLoader)) {
            Class<? extends DistributionProvider> distributionProviderClass = classLoader.loadClass(distributionProvider).asSubclass(DistributionProvider.class);

            Thread.currentThread().setContextClassLoader(classLoader);
            DistributionProvider distributionProvider = distributionProviderClass.getConstructor().newInstance();
            return distributionProvider.split(testClasses, parameters);
        } catch (IOException | ReflectiveOperationException e) {
            throw new MojoExecutionException("Failed to split test classes", e);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    private String getModuleName() {
        return project.getGroupId() + ":" + project.getArtifactId();
    }

    private List<URL> getClasspathUrls() {
        Collection<File> classpath = new TreeSet<>();
        for (Artifact artifact : project.getArtifacts()) {
            // include artifacts in all scopes (provided, compile, runtime, test)
            classpath.add(artifact.getFile());
        }
        classpath.add(classesDirectory);
        classpath.add(testClassesDirectory);
        return classpath.stream().map(SplitMojo::url).collect(Collectors.toList());
    }

    private DefaultScanResult scanTestClassesDirectory() {
        DirectoryScanner scanner = new DirectoryScanner(testClassesDirectory, new TestListResolver(includes, excludes));
        return scanner.scan();
    }

    private static URL url(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }
}
