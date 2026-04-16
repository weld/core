package org.jboss.weld.tests.bce.syntheticInjectionPoint.broken;

import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticInjections;

/**
 * Attempts get(Class) for a type that exists but was not registered
 * via withInjectionPoint().
 */
public class UnregisteredLookupCreator implements SyntheticBeanCreator<UnregisteredClassResult> {
    @Override
    public UnregisteredClassResult create(SyntheticInjections injections, Parameters params) {
        injections.get(ExistingDefaultBean.class);
        return new UnregisteredClassResult();
    }
}
