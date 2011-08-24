package org.jboss.weld.osgi.examples.userdoc.helloworld.helloworld.multilingual;

import org.jboss.weld.osgi.examples.userdoc.helloworld.helloworld.multilingual.api.HelloWorld;
import org.jboss.weld.osgi.examples.userdoc.helloworld.helloworld.multilingual.api.Language;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.jboss.weld.environment.osgi.api.events.BundleContainerEvents;

public class App {

    @Inject @Language("ENGLISH")
    HelloWorld helloWorldEnglish;

    @Inject @Language("FRENCH")
    HelloWorld helloWorldFrench;

    @Inject @Language("GERMAN")
    HelloWorld helloWorldGerman;

    public void onStartup(@Observes BundleContainerEvents.BundleContainerInitialized event) {
        helloWorldEnglish.sayHello();
        helloWorldFrench.sayHello();
        helloWorldGerman.sayHello();
    }

    public void onShutdown(@Observes BundleContainerEvents.BundleContainerShutdown event) {
        helloWorldEnglish.sayGoodbye();
        helloWorldFrench.sayGoodbye();
        helloWorldGerman.sayGoodbye();
    }
}
