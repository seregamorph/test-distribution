package com.github.seregamorph.testdistribution;

import java.util.List;

/**
 * @author Sergey Chernov
 */
public interface DistributionProvider {

    /**
     * Distribute test classes to groups. Each subgroup should preserve original order of test classes.
     *
     * @param testClasses
     * @param numGroups
     * @return list of size numGroups containing subsets of original testClasses
     */
    List<List<String>> split(List<String> testClasses, int numGroups);
}
