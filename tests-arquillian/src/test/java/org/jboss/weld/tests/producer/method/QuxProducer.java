package org.jboss.weld.tests.producer.method;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;

@RequestScoped
public class QuxProducer {

    @Produces
    @Baz
    @RequestScoped
    public Qux getQux() {
        return new Qux("baz");
    }

    public boolean ping() {
        return true;
    }

}
