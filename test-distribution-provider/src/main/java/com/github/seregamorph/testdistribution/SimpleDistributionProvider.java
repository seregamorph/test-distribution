package com.github.seregamorph.testdistribution;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple distribution provider which splits the original list to N groups.
 *
 * @author Sergey Chernov
 */
public class SimpleDistributionProvider implements DistributionProvider {

    @Override
    public List<List<String>> split(List<String> testClasses, TestDistributionParameters parameters) {
        int numGroups = parameters.getNumGroups();
        if (numGroups < 1) {
            throw new IllegalArgumentException("Number of groups must be greater than 0, passed: " + numGroups);
        }

        int groupSize = testClasses.size() / numGroups;
        int mod = testClasses.size() % numGroups;
        if (mod > 0) {
            groupSize++;
        }

        // this distribution always starts from the first bucket
        List<List<String>> groups = new ArrayList<>();
        for (int i = 0; i < numGroups; i++) {
            groups.add(testClasses.subList(i * groupSize, Math.min((i + 1) * groupSize, testClasses.size())));
        }

        int bucketOffset = parameters.getModuleBucket();
        List<List<String>> result = new ArrayList<>();
        result.addAll(groups.subList(bucketOffset, groups.size()));
        result.addAll(groups.subList(0, bucketOffset));

        return result;
    }
}
