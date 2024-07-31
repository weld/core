package org.jboss.weld.tests.accessibility.bce.lib;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;

public class MyBce implements BuildCompatibleExtension {

    @Synthesis
    public void registerSynthBean(SyntheticComponents syntheticComponents) {
        syntheticComponents.addBean(SomeType.class)
                .type(SomeType.class)
                .createWith(MyBeanCreator.class)
                .scope(Dependent.class);
    }
}
