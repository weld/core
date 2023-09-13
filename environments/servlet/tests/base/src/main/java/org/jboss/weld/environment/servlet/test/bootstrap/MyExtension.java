package org.jboss.weld.environment.servlet.test.bootstrap;

import static org.jboss.weld.environment.servlet.test.bootstrap.EventHolder.events;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

public class MyExtension implements Extension {

    public void observeAfterBeanDiscovery(@Observes AfterBeanDiscovery event) {
        events.add(event);
    }

    public void observeAfterDeploymentValidation(@Observes AfterDeploymentValidation event) {
        events.add(event);
    }

    public void observeBeforeBeanDiscovery(@Observes BeforeBeanDiscovery event) {
        events.add(event);
    }

}
