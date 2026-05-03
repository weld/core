package org.jboss.weld.tests.bce.syntheticInjectionPoint.dependentLifecycle;

import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanDisposer;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticInjections;

public class LifecycleDisposer implements SyntheticBeanDisposer<SyntheticBean> {
    @Override
    public void dispose(SyntheticBean instance, SyntheticInjections injections, Parameters params) {
        SyntheticResultHolder.disposerDestroyedCountBeforeGet = DependentCounter.destroyedCounter.get();
        DependentCounter counter = injections.get(DependentCounter.class);
        SyntheticResultHolder.disposerCounterId = counter.getId();
    }
}
