package org.jboss.weld.osgi.examples.userdoc.welcoming.tom;


import javax.enterprise.event.Observes;
import org.jboss.weld.environment.osgi.api.events.BundleContainerEvents;
import org.jboss.weld.environment.osgi.api.events.BundleEvents;

public class App {

    public void onStartup(@Observes BundleContainerEvents.BundleContainerInitialized event) {
        System.out.println("Tom: Hi everyone!");
    }

    public void onShutdown(@Observes BundleContainerEvents.BundleContainerShutdown event) {
        System.out.println("Tom: Bye everyone!");
    }

    public void greetNewcomer(@Observes BundleEvents.BundleStarted event) {
        String name = event.getSymbolicName().substring(21, event.getSymbolicName().length());
        if (!name.equals("tom")) {
            System.out.println("Tom: Welcome " + name +'!');
        }
    }

    public void sayGoodbyeToLeaver(@Observes BundleEvents.BundleStopped event) {
        String name = event.getSymbolicName().substring(21, event.getSymbolicName().length());
        if (!name.equals("tom")) {
            System.out.println("Tom: Goodbye " + name +'!');
        }
    }
}
