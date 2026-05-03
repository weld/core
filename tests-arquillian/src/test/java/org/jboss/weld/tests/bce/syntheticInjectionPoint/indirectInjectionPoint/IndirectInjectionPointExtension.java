package org.jboss.weld.tests.bce.syntheticInjectionPoint.indirectInjectionPoint;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;

public class IndirectInjectionPointExtension implements BuildCompatibleExtension {
    @Synthesis
    public void synthesize(SyntheticComponents syn) {
        syn.addBean(SyntheticPojo.class)
                .type(SyntheticPojo.class)
                .scope(Dependent.class)
                .withInjectionPoint(InjectionPointCaptor.class)
                .createWith(IndirectInjectionPointCreator.class);
    }
}
