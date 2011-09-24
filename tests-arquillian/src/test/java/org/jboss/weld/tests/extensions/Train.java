package org.jboss.weld.tests.extensions;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;

public class Train {

    @Produces
    public Driver getDriver() {
        return new Driver("Nik");
    }

    @Produces
    Ferret ferret = new Ferret("Sammy");

    @Produces
    @Alternative
    public Stoker getStoker() {
        return new Stoker("Stuart");
    }

    @Produces
    @Alternative
    Rabbit rabbit = new Rabbit("Melanie");

    @Produces
    @Relief
    public Guard getGuard() {
        return new Guard("Marius");
    }

    @Produces
    @Relief
    Weasel weasel = new Weasel("Maureen");

    public void observe(@Observes CoalSupply coalSupply) {

    }

}
