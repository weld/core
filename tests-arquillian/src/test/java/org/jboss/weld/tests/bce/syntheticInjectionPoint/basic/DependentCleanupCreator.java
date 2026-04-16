package org.jboss.weld.tests.bce.syntheticInjectionPoint.basic;

import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticInjections;

/**
 * Looks up a @Dependent bean via SyntheticInjections during creation.
 * The dependent instance should be destroyed when this synthetic bean is destroyed.
 */
public class DependentCleanupCreator implements SyntheticBeanCreator<SyntheticResult> {
    @Override
    public SyntheticResult create(SyntheticInjections injections, Parameters params) {
        DependentHelper helper = injections.get(DependentHelper.class);
        return new SyntheticResult("dependentCleanup:" + helper.ping());
    }
}
