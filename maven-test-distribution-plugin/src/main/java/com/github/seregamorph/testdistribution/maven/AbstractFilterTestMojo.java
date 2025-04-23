package com.github.seregamorph.testdistribution.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

/**
 * The basic goal reads generated test distribution part and applies according filtering property.
 *
 * @author Sergey Chernov
 */
public abstract class AbstractFilterTestMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}")
    private File buildDir;

    @Parameter(required = true, property = "testDistribution.testGroupName")
    private String testGroupName;

    @Parameter(required = true, property = "testDistribution.groupNumber")
    private int groupNumber;

    @Override
    public void execute() {
        File testDistributionFile = new File(buildDir, "test-distribution-" + testGroupName + ".json");
        getLog().info("Reading test distribution file: " + testDistributionFile);
        TestDistributionEntity entity = JsonUtils.readEntity(testDistributionFile);
        TestGroupEntity testGroup = entity.getGroups().get(groupNumber - 1);
        List<String> testClasses = testGroup.getTestClasses();
        String projectName = project.getGroupId() + ":" + project.getArtifactId();
        getLog().info("Filtering test group " + projectName + " " + testGroup.getName() + ": "
                + (testClasses.isEmpty() ? "none" : testClasses));

        String testFilter = String.join(",", testClasses);
        if (testFilter.isEmpty()) {
            // Empty property is ignored, so we need to use "skipTests" or "skipITs"
            project.getProperties().put(getSkipTestPropertyName(), "true");
        } else {
            project.getProperties().put(getTestFilterPropertyName(), testFilter);
        }
    }

    abstract String getTestFilterPropertyName();

    abstract String getSkipTestPropertyName();
}
