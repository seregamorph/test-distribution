package com.github.seregamorph.testdistribution;

import java.io.File;

/**
 * @author Sergey Chernov
 */
public class TestDistributionParameters {

    private final int numGroups;
    private final String moduleName;
    private final File modulePath;
    private final int minGroupSize;

    public TestDistributionParameters(int numGroups, String moduleName, File modulePath, int minGroupSize) {
        this.numGroups = numGroups;
        this.moduleName = moduleName;
        this.modulePath = modulePath;
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

    public File getModulePath() {
        return modulePath;
    }

    public int getMinGroupSize() {
        return minGroupSize;
    }

    @Override
    public String toString() {
        return "TestDistributionParameters{" +
                "numGroups=" + numGroups +
                ", moduleName='" + moduleName + '\'' +
                ", modulePath=" + modulePath +
                ", minGroupSize=" + minGroupSize +
                '}';
    }
}
