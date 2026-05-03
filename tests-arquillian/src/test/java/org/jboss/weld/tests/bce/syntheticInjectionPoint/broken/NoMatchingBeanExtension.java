package org.jboss.weld.tests.bce.syntheticInjectionPoint.broken;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;

/**
 * Tests withInjectionPoint for a type that has no matching bean.
 */
public class NoMatchingBeanExtension implements BuildCompatibleExtension {
    @Synthesis
    public void register(SyntheticComponents syn) {
        syn.addBean(String.class)
                .type(String.class)
                .scope(ApplicationScoped.class)
                .withInjectionPoint(NoSuchBean.class)
                .createWith(DummyCreator.class);
    }
}
