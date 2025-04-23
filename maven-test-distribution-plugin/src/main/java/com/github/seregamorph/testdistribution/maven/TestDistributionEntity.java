package com.github.seregamorph.testdistribution.maven;

import java.util.List;

public class TestDistributionEntity {

    private String moduleName;
    private String distributionProvider;
    private int numGroups;
    private List<TestGroupEntity> groups;

    public String getModuleName() {
        return moduleName;
    }

    public TestDistributionEntity setModuleName(String moduleName) {
        this.moduleName = moduleName;
        return this;
    }

    public String getDistributionProvider() {
        return distributionProvider;
    }

    public TestDistributionEntity setDistributionProvider(String distributionProvider) {
        this.distributionProvider = distributionProvider;
        return this;
    }

    public int getNumGroups() {
        return numGroups;
    }

    public TestDistributionEntity setNumGroups(int numGroups) {
        this.numGroups = numGroups;
        return this;
    }

    public List<TestGroupEntity> getGroups() {
        return groups;
    }

    public TestDistributionEntity setGroups(List<TestGroupEntity> groups) {
        this.groups = groups;
        return this;
    }
}
