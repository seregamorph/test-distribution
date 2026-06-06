package com.github.seregamorph.testdistribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
                groups.add(new ArrayList<>(testClasses.subList(fromInclusive, toExclusive)));
            } else {
                groups.add(Collections.emptyList());
            }
        }

        // enclosing class name -> first group containing it
        Map<String, List<String>> firstEnclosingGroup = new TreeMap<>();
        for (List<String> group : groups) {
            for (Iterator<String> iterator = group.iterator(); iterator.hasNext(); ) {
                String testClassName = iterator.next();
                String enclosingTestClassName = getEnclosingClassName(testClassName);
                List<String> firstEclosingGroup = firstEnclosingGroup.get(enclosingTestClassName);
                if (firstEclosingGroup == null) {
                    firstEnclosingGroup.put(enclosingTestClassName, group);
                } else if (firstEclosingGroup != group) {
                    // put all classes with the same enclosing class to the same group
                    // we need it to properly support behavior of surefire plugin
                    iterator.remove();
                    firstEclosingGroup.add(testClassName);
                }
            }
        }

        // this distribution always shifts the first bucket
        int bucketOffset = parameters.getModuleShift();
        List<List<String>> result = new ArrayList<>();
        result.addAll(groups.subList(bucketOffset, groups.size()));
        result.addAll(groups.subList(0, bucketOffset));

        return result;
    }

    static String getEnclosingClassName(String fullClassName) {
        int lastDot = fullClassName.lastIndexOf('.');
        int firstDollar = fullClassName.indexOf('$', lastDot + 1);
        if (firstDollar == -1) {
            return fullClassName;
        }
        return fullClassName.substring(0, firstDollar);
    }
}
