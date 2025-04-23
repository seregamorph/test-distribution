package com.github.seregamorph.testdistribution;

import java.util.List;

/**
 * Test distribution provider implements logic of test suite classes split.
 *
 * @author Sergey Chernov
 */
public interface DistributionProvider {

    /**
     * Distribute test classes to groups. Each subgroup should preserve the original order of test classes.
     *
     * @param testClassNames
     * @param parameters
     * @return list of size numGroups containing subsets of original testClasses
     */
    List<List<String>> split(List<String> testClassNames, TestDistributionParameters parameters);
}
