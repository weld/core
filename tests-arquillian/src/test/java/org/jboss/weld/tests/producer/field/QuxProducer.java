package org.jboss.weld.tests.producer.field;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

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
