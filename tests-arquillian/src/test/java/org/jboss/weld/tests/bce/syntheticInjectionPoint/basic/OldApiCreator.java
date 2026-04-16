package org.jboss.weld.tests.bce.syntheticInjectionPoint.basic;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;

/**
 * Uses the old (deprecated) create(Instance, Parameters) API.
 */
public class OldApiCreator implements SyntheticBeanCreator<SyntheticResult> {
    @Override
    public SyntheticResult create(Instance<Object> lookup, Parameters params) {
        Alpha alpha = lookup.select(Alpha.class).get();
        return new SyntheticResult("old:" + alpha.ping());
    }
}
