package com.github.seregamorph.testdistribution.maven;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.seregamorph.testdistribution.DistributionProvider;
import com.github.seregamorph.testdistribution.SimpleDistributionProvider;
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
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This goal splits module test classes according to chosen {@link DistributionProvider}.
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

    @Parameter(required = true, property = "testdistribution.includes")
    private List<String> includes;

    @Parameter(property = "testdistribution.excludes")
    private List<String> excludes;

    @Parameter(property = "testdistribution.initialSort")
    private boolean initialSort = true;

    @Parameter(property = "testdistribution.distributionProvider")
    private String distributionProvider = SimpleDistributionProvider.class.getName();

    @Parameter(required = true, property = "testdistribution.groupNamePrefix")
    private String groupNamePrefix;

    @Parameter(required = true, property = "testdistribution.numGroups")
    private int numGroups;

    @Override
    public void execute() throws MojoExecutionException {
        if (includes.isEmpty()) {
            throw new MojoExecutionException("Plugin configuration should declare `includes` parameter with class name filtering");
        }

        if (numGroups <= 0) {
            throw new MojoExecutionException("Plugin configuration should declare `numGroups` parameter with value greater than 0");
        }

        getLog().info("Distributing test classes from " + testClassesDirectory + ", includes " + includes
                + (excludes.isEmpty() ? "" : ", excludes " + excludes) + " to " + numGroups + " groups");

        DefaultScanResult scanResult = scanTestClassesDirectory();

        Collection<URL> urls = getClasspathUrls();
        List<String> classes = new ArrayList<>(scanResult.getClasses());
        if (initialSort) {
            Collections.sort(classes);
        }
        List<List<String>> testClassesGroups = splitTestClasses(urls, classes, numGroups);
        List<TestGroupEntity> testGroups = new ArrayList<>();
        for (int i = 0; i < testClassesGroups.size(); i++) {
            List<String> testClasses = testClassesGroups.get(i);
            String groupName = groupNamePrefix + (i + 1);
            getLog().info("Test group " + groupName + ": " + testClasses);
            testGroups.add(new TestGroupEntity().setName(groupName).setTestClasses(testClasses));
        }
        TestDistributionEntity entity = new TestDistributionEntity()
                .setDistributionProvider(distributionProvider)
                .setNumGroups(numGroups)
                .setGroups(testGroups);

        ObjectMapper mapper = createObjectMapper();
        File testDistributionFile = new File(buildDir, "test-distribution.json");
        try {
            mapper.writeValue(testDistributionFile, entity);
        } catch (IOException e) {
            throw new UncheckedIOException("Error while generating " + testDistributionFile, e);
        }
    }

    private List<List<String>> splitTestClasses(Collection<URL> urls, List<String> classes, int numGroups) throws MojoExecutionException {
        // todo support fork option
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]))) {
            Class<?> distributionProviderClass = classLoader.loadClass(distributionProvider);
            Method splitMethod = distributionProviderClass.getMethod("split", List.class, int.class);
            Thread.currentThread().setContextClassLoader(classLoader);
            Object distributionProvider = distributionProviderClass.getConstructor().newInstance();
            //noinspection unchecked
            return (List<List<String>>) splitMethod.invoke(distributionProvider, classes, numGroups);
        } catch (IOException | ReflectiveOperationException e) {
            throw new MojoExecutionException("Failed to split test classes", e);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
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

    private static ObjectMapper createObjectMapper() {
        return new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .setDefaultPrettyPrinter(createPrettyPrinter());
    }

    private static PrettyPrinter createPrettyPrinter() {
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("  ", DefaultIndenter.SYS_LF);
        prettyPrinter.indentArraysWith(indenter);
        return prettyPrinter;
    }

    private static URL url(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }
}
