package org.jboss.weld.tests.bce.syntheticInjectionPoint.broken;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;

public class BothMethodsExtension implements BuildCompatibleExtension {
    @Synthesis
    public void register(SyntheticComponents syn) {
        syn.addBean(String.class)
                .type(String.class)
                .scope(ApplicationScoped.class)
                .createWith(BothMethodsCreator.class);
    }
}
