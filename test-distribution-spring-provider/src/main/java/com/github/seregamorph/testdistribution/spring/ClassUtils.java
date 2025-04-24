package com.github.seregamorph.testdistribution.spring;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sergey Chernov
 */
final class ClassUtils {

    static List<Class<?>> classNamesToClasses(List<String> classNames, boolean initialize) {
        return classNames.stream()
                .map(testClassName -> classForName(testClassName, initialize,
                        SpringDistributionProvider.class.getClassLoader()))
                .collect(Collectors.toList());
    }

    private static Class<?> classForName(String className, boolean initialize, ClassLoader classLoader) {
        try {
            return Class.forName(className, initialize, classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load class [" + className + "] with classLoader " + className);
        }
    }

    static List<String> classesToClassNames(List<Class<?>> classes) {
        return classes.stream()
                .map(Class::getName)
                .collect(Collectors.toList());
    }

    private ClassUtils() {
    }
}
