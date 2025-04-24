package com.github.seregamorph.testdistribution.maven;

import java.util.List;

/**
 * @author Sergey Chernov
 */
public class TestGroupEntity {

    private String name;
    private List<String> testClasses;

    public String getName() {
        return name;
    }

    public TestGroupEntity setName(String name) {
        this.name = name;
        return this;
    }

    public List<String> getTestClasses() {
        return testClasses;
    }

    public TestGroupEntity setTestClasses(List<String> testClasses) {
        this.testClasses = testClasses;
        return this;
    }
}
