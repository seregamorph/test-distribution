package com.github.seregamorph.testdistribution.spring;

import static java.util.Comparator.comparing;

import com.github.seregamorph.testdistribution.DistributionProvider;
import com.github.seregamorph.testdistribution.SimpleDistributionProvider;
import com.github.seregamorph.testdistribution.TestDistributionParameters;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.BootstrapUtilsHelper;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextBootstrapper;

/**
 * Test class distribution provider for Spring Integration tests based on calculated MergedContextConfiguration.
 * <p>
 * See more details <a href="https://github.com/seregamorph/spring-test-smart-context">spring-test-smart-context</a>
 *
 * @author Sergey Chernov
 */
public class SpringDistributionProvider implements DistributionProvider {

    private static final Logger logger = LoggerFactory.getLogger(SpringDistributionProvider.class);

    @Override
    public List<List<String>> split(List<String> testClassNames, TestDistributionParameters parameters) {
        if (testClassNames.size() <= parameters.getMinGroupSize()) {
            if (!testClassNames.isEmpty()) {
                logger.info("Using default distribution for {} test classes", testClassNames.size());
            }
            return new SimpleDistributionProvider().split(testClassNames, parameters);
        }

        try {
            Class.forName("org.springframework.test.context.BootstrapUtils", true,
                    SpringDistributionProvider.class.getClassLoader());
            Class.forName("org.springframework.context.ApplicationContextAware", true,
                    SpringDistributionProvider.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            logger.warn("Missing spring framework in the classpath, fallback to default provider");
            return new SimpleDistributionProvider().split(testClassNames, parameters);
        }

        List<Class<?>> testClasses = ClassUtils.classNamesToClasses(testClassNames, false);
        Set<Class<?>> itClasses = filterItClasses(testClasses);

        Map<MergedContextConfiguration, TestClasses> configToTests = new LinkedHashMap<>();
        Map<Class<?>, Integer> classToOrder = new LinkedHashMap<>();
        AtomicInteger orderCounter = new AtomicInteger();
        for (Class<?> itClass : itClasses) {
            TestContextBootstrapper bootstrapper = BootstrapUtilsHelper.resolveTestContextBootstrapper(itClass);
            MergedContextConfiguration mergedContextConfiguration = bootstrapper.buildMergedContextConfiguration();
            // Sequentially each unique mergedContextConfiguration will have own order
            // via orderCounter. initial order values all have a gap to allow for moving
            // @DirtiesContext annotated classes to the back later.
            TestClasses configurationTestClasses = configToTests.computeIfAbsent(mergedContextConfiguration,
                    $ -> new TestClasses(orderCounter.incrementAndGet(), new LinkedHashSet<>()));
            configurationTestClasses.classes.add(itClass);
            classToOrder.put(itClass, configurationTestClasses.order);
        }

        if (configToTests.isEmpty()) {
            logger.info("Splitting {} test classes, none of them are integration tests", testClasses.size());
        } else {
            logger.info("Splitting {} test classes, {} IT classes are detected with {} separate configurations",
                    testClasses.size(), itClasses.size(), configToTests.size());
        }

        List<Class<?>> sortedTestClasses = testClasses.stream().sorted(comparing(testClass -> {
            Integer order = classToOrder.get(testClass);
            if (order == null) {
                // all non-IT tests go first (other returned values are non-zero)
                // this logic can be changed via override
                return getNonItOrder();
            } else {
                // this sorting is stable - most of the tests will preserve alphabetical ordering where possible
                // we only sort classes that shut down the context to the end.
                return order;
            }
        })).collect(Collectors.toList());

        return new SimpleDistributionProvider().split(ClassUtils.classesToClassNames(sortedTestClasses), parameters);
    }

    /**
     * Get the order of non-integration test execution (bigger is later). Can be either first or last. 0 (first) by
     * default.
     */
    protected int getNonItOrder() {
        return 0;
    }

    private static Set<Class<?>> filterItClasses(List<Class<?>> testClasses) {
        IntegrationTestFilter integrationTestFilter = IntegrationTestFilter.getInstance();
        Set<Class<?>> itClasses = new LinkedHashSet<>();
        for (Class<?> testClass : testClasses) {
            if (!itClasses.contains(testClass) && integrationTestFilter.isIntegrationTest(testClass)) {
                itClasses.add(testClass);
            }
        }
        return itClasses;
    }

    private static final class TestClasses {

        private final int order;
        private final Set<Class<?>> classes;

        private TestClasses(int order, Set<Class<?>> classes) {
            this.order = order;
            this.classes = classes;
        }
    }
}
