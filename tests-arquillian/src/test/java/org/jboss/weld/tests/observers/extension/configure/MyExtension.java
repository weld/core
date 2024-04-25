package org.jboss.weld.tests.observers.extension.configure;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Reception;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessObserverMethod;

public class MyExtension implements Extension {

    public void pom(@Observes ProcessObserverMethod<Foo, ObservingBean> pom) {
        pom.configureObserverMethod().reception(Reception.IF_EXISTS);
    }
}
