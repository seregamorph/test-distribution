package com.github.seregamorph.testdistribution.spring;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Sergey Chernov
 */
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface SpringTestDistribution {

    /**
     * "Cost" multiplier of the separate context class initialization.
     *
     * 1 by default, may be increased to a higher number for integration tests with heavy context initialization.
     */
    int separateContextClassMultiplier() default 1;
}
