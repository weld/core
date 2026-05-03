package org.jboss.weld.tests.autoclose.synthetic;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;

public class AutoCloseBceExtension implements BuildCompatibleExtension {

    @Synthesis
    public void register(SyntheticComponents syn) {
        syn.addBean(SyntheticCloseableResource.class)
                .type(SyntheticCloseableResource.class)
                .qualifier(BceQualifier.class)
                .scope(Dependent.class)
                .autoClose(true)
                .createWith(AutoCloseBceCreator.class);
    }
}
