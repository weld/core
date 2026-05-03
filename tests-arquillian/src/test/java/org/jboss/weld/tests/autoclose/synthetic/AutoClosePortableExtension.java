package org.jboss.weld.tests.autoclose.synthetic;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

import org.jboss.weld.test.util.ActionSequence;

public class AutoClosePortableExtension implements Extension {

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event) {
        event.addBean()
                .addType(SyntheticCloseableResource.class)
                .addQualifier(PortableExtQualifier.Literal.INSTANCE)
                .scope(Dependent.class)
                .autoClose(true)
                .produceWith(lookup -> new SyntheticCloseableResource("portableExt"));

        event.addBean()
                .addType(SyntheticCloseableResource.class)
                .addQualifier(WithDisposerQualifier.Literal.INSTANCE)
                .scope(Dependent.class)
                .autoClose(true)
                .produceWith(lookup -> new SyntheticCloseableResource("withDisposer"))
                .disposeWith((instance, lookup) -> ActionSequence.addAction("syntheticDisposer"));
    }
}
