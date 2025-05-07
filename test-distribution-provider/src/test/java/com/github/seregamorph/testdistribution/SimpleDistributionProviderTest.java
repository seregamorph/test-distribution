package com.github.seregamorph.testdistribution;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleDistributionProviderTest {

    private static final DistributionProvider DISTRIBUTION_PROVIDER = new SimpleDistributionProvider();

    private static final File modulePath = new File("");

    @Test
    public void shouldSplitMinSize1() {
        assertEquals(list(list()), DISTRIBUTION_PROVIDER.split(list(), params1(1)));
        assertEquals(list(list(), list()), DISTRIBUTION_PROVIDER.split(list(), params1(2)));
        assertEquals(list(list("test1"), list()), DISTRIBUTION_PROVIDER.split(list("test1"), params1(2)));
        assertEquals(list(list("test1"), list("test2")), DISTRIBUTION_PROVIDER.split(list("test1", "test2"), params1(2)));
        assertEquals(list(list("test1", "test2"), list("test3")), DISTRIBUTION_PROVIDER.split(list("test1", "test2", "test3"), params1(2)));
        assertEquals(list(list("test2"), list("test3"), list("test1")), DISTRIBUTION_PROVIDER.split(list("test1", "test2", "test3"), params1(3)));
        assertEquals(list(list("test3"), list(), list("test1"), list("test2")), DISTRIBUTION_PROVIDER.split(list("test1", "test2", "test3"), params1(4)));
    }

    @Test
    public void shouldSplitMinSize2() {
        assertEquals(list(list()), DISTRIBUTION_PROVIDER.split(list(), params3(1)));
        assertEquals(list(list(), list()), DISTRIBUTION_PROVIDER.split(list(), params3(2)));
        assertEquals(list(list("test1"), list()), DISTRIBUTION_PROVIDER.split(list("test1"), params3(2)));
        assertEquals(list(list("test1", "test2"), list()), DISTRIBUTION_PROVIDER.split(list("test1", "test2"), params3(2)));
        assertEquals(list(list("test1", "test2", "test3"), list()), DISTRIBUTION_PROVIDER.split(list("test1", "test2", "test3"), params3(2)));
        assertEquals(list(list(), list(), list("test1", "test2", "test3")), DISTRIBUTION_PROVIDER.split(list("test1", "test2", "test3"), params3(3)));
        assertEquals(list(list(), list(), list("test1", "test2", "test3"), list()), DISTRIBUTION_PROVIDER.split(list("test1", "test2", "test3"), params3(4)));
    }

    @SafeVarargs
    private static <T> List<T> list(T... elements) {
        return List.of(elements);
    }

    private static TestDistributionParameters params1(int numGroups) {
        return new TestDistributionParameters(numGroups, "test", modulePath, 1);
    }

    private static TestDistributionParameters params3(int numGroups) {
        return new TestDistributionParameters(numGroups, "test", modulePath, 3);
    }
}
