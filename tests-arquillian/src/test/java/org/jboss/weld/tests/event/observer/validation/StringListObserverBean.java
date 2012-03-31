package org.jboss.weld.tests.event.observer.validation;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class StringListObserverBean {

    private List<String> data;
    private boolean receivedUpdate;

    public void listenForEvent(@Observes final List<String> data) {
        this.data = data;
        this.receivedUpdate = true;
    }

    public List<String> getData() {
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
