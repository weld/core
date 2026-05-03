package org.jboss.weld.tests.bce.syntheticInjectionPoint.dependentLifecycle;

import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticInjections;

public class LifecycleCreator implements SyntheticBeanCreator<SyntheticBean> {
    @Override
    public SyntheticBean create(SyntheticInjections injections, Parameters params) {
        DependentCounter counter = injections.get(DependentCounter.class);
        SyntheticResultHolder.creatorCounterId = counter.getId();
        return new SyntheticBean("created");
    }
}
