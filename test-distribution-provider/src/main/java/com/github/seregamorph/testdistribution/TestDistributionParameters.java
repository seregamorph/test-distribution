package com.github.seregamorph.testdistribution;

/**
 * @author Sergey Chernov
 */
public class TestDistributionParameters {

    private final int numGroups;
    private final String moduleName;
    private final int minGroupSize;

    public TestDistributionParameters(int numGroups, String moduleName, int minGroupSize) {
        this.numGroups = numGroups;
        this.moduleName = moduleName;
        this.minGroupSize = minGroupSize;
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
    public int getModuleShift() {
        return Math.abs(moduleName.hashCode() % numGroups);
    }

    public int getMinGroupSize() {
        return minGroupSize;
    }

    @Override
    public String toString() {
        return "TestDistributionParameters{" +
                "numGroups=" + numGroups +
                ", moduleName='" + moduleName + '\'' +
                ", minGroupSize=" + minGroupSize +
                '}';
    }
}
