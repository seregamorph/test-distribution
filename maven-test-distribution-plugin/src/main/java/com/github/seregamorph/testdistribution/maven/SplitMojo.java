package com.github.seregamorph.testdistribution.maven;

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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    @Parameter(defaultValue = "${project.build.outputDirectory}")
    private File classesDirectory;

    @Parameter(defaultValue = "${project.build.testOutputDirectory}")
    private File testClassesDirectory;

    @Parameter(required = true, property = "testdistribution.includes")
    private List<String> includes;

    @Parameter(property = "testdistribution.excludes")
    private List<String> excludes;

    @Parameter(property = "testdistribution.distributionGenerator")
    private String distributionGenerator;

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

        getLog().info("Distributing test classes from " + testClassesDirectory + ", includes " + includes);

        DefaultScanResult scanResult = scanTestClassesDirectory();

        try {
            List<List<String>> testClassesGroups = splitTestClasses(scanResult.getClasses(), numGroups);
            // todo store
            testClassesGroups.forEach(System.out::println);
        } catch (IOException | ReflectiveOperationException e) {
            throw new MojoExecutionException("Failed to split test classes", e);
        }
    }

    private List<List<String>> splitTestClasses(List<String> classes, int numGroups) throws IOException, ReflectiveOperationException {
        if (distributionGenerator == null || distributionGenerator.isEmpty()) {
            return new SimpleDistributionProvider().split(classes, numGroups);
        }

        Collection<URL> urls = new ArrayList<>();
        for (Artifact artifact : project.getArtifacts()) {
            urls.add(artifact.getFile().toPath().toUri().toURL());
        }
        urls.add(classesDirectory.toURI().toURL());
        urls.add(testClassesDirectory.toURI().toURL());

        // todo support fork option
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]))) {
            Class<? extends DistributionProvider> distributionGeneratorClass =
                    classLoader.loadClass(distributionGenerator).asSubclass(DistributionProvider.class);
            Method splitMethod = distributionGeneratorClass.getMethod("split", List.class, int.class);
            Thread.currentThread().setContextClassLoader(classLoader);
            DistributionProvider distributionProvider = distributionGeneratorClass.getConstructor().newInstance();
            //noinspection unchecked
            return (List<List<String>>) splitMethod.invoke(distributionProvider, classes, numGroups);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    private DefaultScanResult scanTestClassesDirectory() {
        DirectoryScanner scanner = new DirectoryScanner(testClassesDirectory, new TestListResolver(includes, excludes));
        return scanner.scan();
    }
}
