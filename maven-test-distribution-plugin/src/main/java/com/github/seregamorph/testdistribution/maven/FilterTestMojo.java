package com.github.seregamorph.testdistribution.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * This goal reads generated test distribution part and applies as property for subsequent
 * surefire test execution via "test" property.
 *
 * @author Sergey Chernov
 */
@Mojo(
        name = "filter-test",
        requiresDependencyResolution = ResolutionScope.TEST,
        defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES,
        threadSafe = true)
public class FilterTestMojo extends AbstractFilterTestMojo {

    @Override
    String getTestFilterPropertyName() {
        return "test";
    }
}
