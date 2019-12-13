package org.jboss.weld.tests.producer.field;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;

@RequestScoped
public class QuxProducer {

    @Produces
    @Baz
    @RequestScoped
    private Qux bar = new Qux("baz");

    public boolean ping() {
        return true;
    }

}
