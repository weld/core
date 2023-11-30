package org.jboss.weld.tests.producer.alternative.priority.complex;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
@Alternative
@Priority(1)
public class GloballyEnabledAlt {

    @Produces
    @Alternative
    @Priority(50)
    public Foo produceAltFoo() {
        return new Foo(ProducerOnLocallyEnabledAltTest.ALT);
    }

    @Produces
    @Alternative
    @Priority(50)
    public Bar produceAltBar() {
        return new Bar(ProducerOnLocallyEnabledAltTest.ALT);
    }
}
