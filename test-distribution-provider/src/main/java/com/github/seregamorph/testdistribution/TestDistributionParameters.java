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
    private final int shiftOffset;

    public TestDistributionParameters(
        int numGroups,
        String moduleName,
        File modulePath,
        int minGroupSize,
        int shiftOffset
    ) {
        this.numGroups = numGroups;
        this.moduleName = moduleName;
        this.modulePath = modulePath;
        this.minGroupSize = minGroupSize;
        this.shiftOffset = shiftOffset;
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
        return Math.abs((moduleName.hashCode() - shiftOffset) % numGroups);
    }

    public File getModulePath() {
        return modulePath;
    }

    public int getMinGroupSize() {
        return minGroupSize;
    }

    public int getShiftOffset() {
        return shiftOffset;
    }

    @Override
    public String toString() {
        return "TestDistributionParameters{" +
            "numGroups=" + numGroups +
            ", moduleName='" + moduleName + '\'' +
            ", modulePath=" + modulePath +
            ", minGroupSize=" + minGroupSize +
            ", shiftOffset=" + shiftOffset +
            '}';
    }
}
