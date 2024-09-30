package org.jboss.weld.tests.injectionPoint.resource.extension;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterTypeDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;

public class MyExtension implements Extension {

    void afterTypeDiscovery(@Observes AfterTypeDiscovery event, BeanManager bm) {
        event.addAnnotatedType(bm.createAnnotatedType(FooSpecializedNoBeanDef.class),
                FooSpecializedNoBeanDef.class.getName() + "_synth");
    }
}
