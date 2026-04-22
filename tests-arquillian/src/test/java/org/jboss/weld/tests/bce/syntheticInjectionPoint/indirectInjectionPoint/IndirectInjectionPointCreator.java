package org.jboss.weld.tests.bce.syntheticInjectionPoint.indirectInjectionPoint;

import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticInjections;

public class IndirectInjectionPointCreator implements SyntheticBeanCreator<SyntheticPojo> {
    @Override
    public SyntheticPojo create(SyntheticInjections injections, Parameters params) {
        InjectionPointCaptor captor = injections.get(InjectionPointCaptor.class);
        return new SyntheticPojo(captor.injectionPoint);
    }
}
