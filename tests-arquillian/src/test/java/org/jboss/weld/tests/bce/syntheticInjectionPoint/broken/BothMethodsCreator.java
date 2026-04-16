package org.jboss.weld.tests.bce.syntheticInjectionPoint.broken;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticInjections;

/**
 * Implements BOTH create methods — this should be a deployment error.
 */
public class BothMethodsCreator implements SyntheticBeanCreator<String> {
    @Override
    public String create(SyntheticInjections injections, Parameters params) {
        return "new";
    }

    @Override
    public String create(Instance<Object> lookup, Parameters params) {
        return "old";
    }
}
