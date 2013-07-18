package org.jboss.weld.osgi.examples.userdoc.welcoming.harry;


import javax.enterprise.event.Observes;

import org.jboss.weld.environment.osgi.api.events.BundleContainerEvents;
import org.jboss.weld.environment.osgi.api.events.BundleEvents;

public class App {

    private static final int NAME_BEGIN_INDEX = 21;
    private static final String NAME = "harry";

    public void onStartup(@Observes BundleContainerEvents.BundleContainerInitialized event) {
        System.out.println("Harry: Hi everyone!");
    }

    public void onShutdown(@Observes BundleContainerEvents.BundleContainerShutdown event) {
        System.out.println("Harry: Bye everyone!");
    }

    public void greetNewcomer(@Observes BundleEvents.BundleStarted event) {
        String name = event.getSymbolicName().substring(NAME_BEGIN_INDEX, event.getSymbolicName().length());
        if (!name.equals(NAME)) {
            System.out.println("Harry: Welcome " + name +'!');
        }
    }

    public void sayGoodbyeToLeaver(@Observes BundleEvents.BundleStopped event) {
        String name = event.getSymbolicName().substring(NAME_BEGIN_INDEX, event.getSymbolicName().length());
        if (!name.equals(NAME)) {
            System.out.println("Harry: Goodbye " + name +'!');
        }
    }
}
