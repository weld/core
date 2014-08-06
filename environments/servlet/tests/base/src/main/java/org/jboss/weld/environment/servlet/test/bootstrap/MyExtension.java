package org.jboss.weld.environment.servlet.test.bootstrap;

import static org.jboss.weld.environment.servlet.test.bootstrap.EventHolder.events;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

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
