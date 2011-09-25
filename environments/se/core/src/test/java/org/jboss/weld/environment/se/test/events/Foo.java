package org.jboss.weld.environment.se.test.events;

import org.jboss.weld.environment.se.ShutdownManager;
import org.jboss.weld.environment.se.events.ContainerInitialized;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

public class Foo {

    private static boolean observedEventTest;
    private static boolean observedContainerInitialized;

    public static void reset() {
        observedEventTest = false;
        observedContainerInitialized = false;
    }

    public static boolean isObservedContainerInitialized() {
        return observedContainerInitialized;
    }

    public static boolean isObservedEventTest() {
        return observedEventTest;
    }

    @Inject
    @EventQualifier1
    private Event<Bar> eventTest;

    public void start(@Observes ContainerInitialized event, ShutdownManager shutdownManager) {
        eventTest.fire(new Bar());
        observedContainerInitialized = true;
    }

    public void observeEventTest(@Observes @EventQualifier2 Bar eventTest) {
        observedEventTest = true;
    }

}
