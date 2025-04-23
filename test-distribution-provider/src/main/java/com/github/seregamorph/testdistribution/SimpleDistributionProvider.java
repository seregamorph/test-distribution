package com.github.seregamorph.testdistribution;

import java.util.ArrayList;
import java.util.Collections;
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
        if (groupSize < parameters.getMinGroupSize()) {
            groupSize = parameters.getMinGroupSize();
        }

        // this distribution always starts from the first bucket
        List<List<String>> groups = new ArrayList<>();
        for (int i = 0; i < numGroups; i++) {
            int fromInclusive = i * groupSize;
            int toExclusive = (i + 1) * groupSize;
            if (toExclusive > testClasses.size()) {
                toExclusive = testClasses.size();
            }
            if (fromInclusive < toExclusive) {
                groups.add(testClasses.subList(fromInclusive, toExclusive));
            } else {
                groups.add(Collections.emptyList());
            }
        }

        // this distribution always shifts the first bucket
        int bucketOffset = parameters.getModuleShift();
        List<List<String>> result = new ArrayList<>();
        result.addAll(groups.subList(bucketOffset, groups.size()));
        result.addAll(groups.subList(0, bucketOffset));

        return result;
    }
}
