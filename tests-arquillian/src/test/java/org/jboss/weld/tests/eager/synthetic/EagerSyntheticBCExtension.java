package org.jboss.weld.tests.eager.synthetic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;

public class EagerSyntheticBCExtension implements BuildCompatibleExtension {

    @Synthesis
    public void addEagerBean(SyntheticComponents syn) {
        syn.addBean(EagerSyntheticBeanBCE.class)
                .type(EagerSyntheticBeanBCE.class)
                .scope(ApplicationScoped.class)
                .eager(true)
                .createWith(EagerSyntheticBeanBCECreator.class);
    }
}
