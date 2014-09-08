package org.jboss.weld.tests.event.ordering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

public class EventPayload {

    private List<String> observers = new ArrayList<String>();

    EventPayload() {
    }

    void record(String observer) {
        observers.add(observer);
    }

    public void assertObservers(String... expectedObservers) {
        if (observers.size() != expectedObservers.length) {
            fail("observers: " + observers.size() + ", expected: " + expectedObservers.length);
        }
        for (int i = 0; i < expectedObservers.length; i++) {
            assertEquals("Observers at index " + i + "do not match", expectedObservers[i], observers.get(i));
        }
    }

    public void reset() {
        observers.clear();
    }

}
