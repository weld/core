package org.jboss.weld.tests.extensions.lifecycle.processSyntheticAnnotatedType;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterTypeDiscovery;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

public class RegisteringExtension3 implements Extension {

    // using BBD
    void registerJuicyKiwi(@Observes BeforeBeanDiscovery event) {
        event.addAnnotatedType(Kiwi.class, Kiwi.class.getSimpleName() + "Configured").add(Juicy.Literal.INSTANCE);
    }

    // using ATD
    void registerFreshBanana(@Observes AfterTypeDiscovery event) {
        event.addAnnotatedType(Banana.class, Banana.class.getSimpleName() + "Configured").add(Fresh.Literal.INSTANCE);
    }
}
