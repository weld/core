package org.jboss.weld.osgi.examples.standalone;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import org.jboss.weld.environment.osgi.api.events.ServiceEvents.ServiceArrival;
import org.jboss.weld.environment.osgi.api.events.ServiceEvents.ServiceChanged;
import org.jboss.weld.environment.osgi.api.events.ServiceEvents.ServiceDeparture;

@ApplicationScoped
public class MyListener {

    public void listen1(@Observes ServiceArrival evt) {
        System.out.println("service arrival");
    }

    public void listen2(@Observes ServiceDeparture evt) {
        System.out.println("service departure");
    }

    public void listen3(@Observes ServiceChanged evt) {
        System.out.println("service changed");
    }
}
