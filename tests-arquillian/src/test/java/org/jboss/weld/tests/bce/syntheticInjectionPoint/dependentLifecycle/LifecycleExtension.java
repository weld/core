package org.jboss.weld.tests.bce.syntheticInjectionPoint.dependentLifecycle;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;

public class LifecycleExtension implements BuildCompatibleExtension {
    @Synthesis
    public void register(SyntheticComponents syn) {
        syn.addBean(SyntheticBean.class)
                .type(SyntheticBean.class)
                .scope(Dependent.class)
                .withInjectionPoint(DependentCounter.class)
                .createWith(LifecycleCreator.class)
                .disposeWith(LifecycleDisposer.class);
    }
}
