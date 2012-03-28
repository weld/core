package org.jboss.weld.tests.event.observer.validation;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;

@Singleton
public class ArrayObserverBean {

    private int[] data;
    private boolean receivedUpdate;

    public void listenForEvent(@Observes final int[] data) {
        this.data = data;
        this.receivedUpdate = true;
    }

    public int[] getData() {
        return data;
    }

    public boolean isReceivedUpdate() {
        return receivedUpdate;
    }

    void reset() {
        this.data = null;
        this.receivedUpdate = false;
    }

}
