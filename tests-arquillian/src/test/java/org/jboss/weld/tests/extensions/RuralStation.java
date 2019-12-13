package org.jboss.weld.tests.extensions;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;

@Alternative
public class RuralStation implements Station {

    @Produces
    public Passenger getPassenger() {
        return new Passenger("Pete");
    }

    @Produces
    Cat cat = new Cat("George");

    public void observe(@Observes FatController fatController) {

    }

}
