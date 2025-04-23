package com.github.seregamorph.testdistribution;

public class TestDistributionParameters {

    private final int numGroups;
    private final String moduleName;

    public TestDistributionParameters(int numGroups, String moduleName) {
        this.numGroups = numGroups;
        this.moduleName = moduleName;
    }

    public int getNumGroups() {
        return numGroups;
    }

    public String getModuleName() {
        return moduleName;
    }

    /**
     * Module name can be used to shift the first module of the distribution
     */
    public int getModuleBucket() {
        return Math.abs(moduleName.hashCode() % numGroups);
    }

    @Override
    public String toString() {
        return "TestDistributionParameters{" +
                "numGroups=" + numGroups +
                ", moduleName='" + moduleName + '\'' +
                '}';
    }
}
