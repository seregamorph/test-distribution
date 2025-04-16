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
        name = "filter-integration-test",
        requiresDependencyResolution = ResolutionScope.TEST,
        defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST,
        threadSafe = true)
public class FilterIntegrationTestMojo extends AbstractFilterTestMojo {

    @Override
    String getTestFilterPropertyName() {
        return "it.test";
    }
}
