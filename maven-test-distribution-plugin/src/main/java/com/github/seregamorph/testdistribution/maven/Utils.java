package com.github.seregamorph.testdistribution.maven;

import java.util.List;
import java.util.stream.Collectors;

final class Utils {

    static List<String> getSimpleClassNames(List<String> testClasses) {
        return testClasses.stream()
                .map(Utils::getSimpleClassName)
                .collect(Collectors.toList());
    }

    private static String getSimpleClassName(String className) {
        int lastSep = className.lastIndexOf('.');
        return lastSep < 0 ? className : className.substring(lastSep + 1);
    }

    private Utils() {
    }
}
