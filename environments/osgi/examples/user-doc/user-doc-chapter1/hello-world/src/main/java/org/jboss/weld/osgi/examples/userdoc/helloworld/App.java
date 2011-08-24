package org.jboss.weld.osgi.examples.userdoc.helloworld;

import org.jboss.weld.osgi.examples.userdoc.helloworld.api.HelloWorld;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.jboss.weld.environment.osgi.api.events.BundleContainerEvents;

public class App {

    @Inject
    HelloWorld helloWorld;

    public void onStartup(@Observes BundleContainerEvents.BundleContainerInitialized event) {
        helloWorld.sayHello();
    }

    public void onShutdown(@Observes BundleContainerEvents.BundleContainerShutdown event) {
        helloWorld.sayGoodbye();
    }
}
