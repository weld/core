package org.jboss.weld.tests.producer.alternative.priority.complex;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class RegularProducer {

    @Produces
    public Foo produceFoo() {
        return new Foo(ProducerOnLocallyEnabledAltTest.DEFAULT);
    }

    @Produces
    public Bar produceBar() {
        return new Bar(ProducerOnLocallyEnabledAltTest.DEFAULT);
    }
}
