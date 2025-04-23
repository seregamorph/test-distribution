package com.github.seregamorph.testdistribution;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleDistributionProviderTest {

    private static final DistributionProvider DISTRIBUTION_PROVIDER = new SimpleDistributionProvider();

    @Test
    public void shouldSplit() {
        assertEquals(List.of(List.of()), DISTRIBUTION_PROVIDER.split(List.of(), parameters(1)));
        assertEquals(List.of(List.of(), List.of()), DISTRIBUTION_PROVIDER.split(List.of(), parameters(2)));
        assertEquals(List.of(List.of("test1"), List.of()), DISTRIBUTION_PROVIDER.split(List.of("test1"), parameters(2)));
        assertEquals(List.of(List.of("test1"), List.of("test2")), DISTRIBUTION_PROVIDER.split(List.of("test1", "test2"), parameters(2)));
        assertEquals(List.of(List.of("test1", "test2"), List.of("test3")), DISTRIBUTION_PROVIDER.split(List.of("test1", "test2", "test3"), parameters(2)));
        assertEquals(List.of(List.of("test1"), List.of("test2"), List.of("test3")), DISTRIBUTION_PROVIDER.split(List.of("test1", "test2", "test3"), parameters(3)));
        assertEquals(List.of(List.of("test1"), List.of("test2"), List.of("test3"), List.of()), DISTRIBUTION_PROVIDER.split(List.of("test1", "test2", "test3"), parameters(4)));
    }

    private static TestDistributionParameters parameters(int numGroups) {
        return new TestDistributionParameters(numGroups);
    }
}
