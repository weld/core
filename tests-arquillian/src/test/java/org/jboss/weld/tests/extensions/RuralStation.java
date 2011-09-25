package org.jboss.weld.tests.extensions;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;

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
