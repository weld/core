package org.jboss.weld.osgi.examples.userdoc.talkative.dick;

import org.osgi.framework.Bundle;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.jboss.weld.environment.osgi.api.annotation.Sent;
import org.jboss.weld.environment.osgi.api.annotation.Specification;
import org.jboss.weld.environment.osgi.api.events.BundleContainerEvents;
import org.jboss.weld.environment.osgi.api.events.BundleEvents;
import org.jboss.weld.environment.osgi.api.events.InterBundleEvent;

public class App {

    @Inject
    private Event<InterBundleEvent> communication;

    public void onStartup(@Observes BundleContainerEvents.BundleContainerInitialized event) {
        System.out.println("Dick: Hi everyone!");
        AskThread askThread = new AskThread(communication, event.getBundleContext().getBundle());
        askThread.start();
    }

    public void onShutdown(@Observes BundleContainerEvents.BundleContainerShutdown event) {
        System.out.println("Dick: Bye everyone!");
    }

    public void greetNewcomer(@Observes BundleEvents.BundleStarted event) {
        String name = event.getSymbolicName().substring(21, event.getSymbolicName().length());
        if (!name.equals("dick")) {
            System.out.println("Dick: Welcome " + name +'!');
        }
    }

    public void sayGoodbyeToLeaver(@Observes BundleEvents.BundleStopped event) {
        String name = event.getSymbolicName().substring(21, event.getSymbolicName().length());
        if (!name.equals("dick")) {
            System.out.println("Dick: Goodbye " + name +'!');
        }
    }

    public void acknowledge(@Observes @Sent @Specification(String.class) InterBundleEvent message) {
        System.out.println("Dick: Hey " + message.get() + " i'm still here.");
    }

    private class AskThread extends Thread {
        Event<InterBundleEvent> communication;
        Bundle bundle;

        AskThread(Event<InterBundleEvent> communication, Bundle bundle) {
            this.communication = communication;
            this.bundle = bundle;
        }

        public void run() {
            while(true) {
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                }
                if(bundle.getState() == Bundle.ACTIVE) {
                    System.out.println("Dick: is there still someone here ?");
                    communication.fire(new InterBundleEvent("dick"));
                } else {
                    break;
                }
            }
        }
    }
}
