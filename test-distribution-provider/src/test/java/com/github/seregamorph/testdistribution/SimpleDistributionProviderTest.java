package com.github.seregamorph.testdistribution;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleDistributionProviderTest {

    private static final DistributionProvider DISTRIBUTION_PROVIDER = new SimpleDistributionProvider();

    @Test
    public void shouldSplit() {
        assertEquals(list(list()), DISTRIBUTION_PROVIDER.split(list(), parameters(1)));
        assertEquals(list(list(), list()), DISTRIBUTION_PROVIDER.split(list(), parameters(2)));
        assertEquals(list(list("test1"), list()), DISTRIBUTION_PROVIDER.split(list("test1"), parameters(2)));
        assertEquals(list(list("test1"), list("test2")), DISTRIBUTION_PROVIDER.split(list("test1", "test2"), parameters(2)));
        assertEquals(list(list("test1", "test2"), list("test3")), DISTRIBUTION_PROVIDER.split(list("test1", "test2", "test3"), parameters(2)));
        assertEquals(list(list("test2"), list("test3"), list("test1")), DISTRIBUTION_PROVIDER.split(list("test1", "test2", "test3"), parameters(3)));
        assertEquals(list(list("test3"), list(), list("test1"), list("test2")), DISTRIBUTION_PROVIDER.split(list("test1", "test2", "test3"), parameters(4)));
    }

    @SafeVarargs
    private static <T> List<T> list(T... elements) {
        return List.of(elements);
    }

    private static TestDistributionParameters parameters(int numGroups) {
        return new TestDistributionParameters(numGroups, "test");
    }
}
