package org.jboss.weld.tests.producer.alternative.priority.complex;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
@Alternative
// priority intentionally missing, enabled via beans.xml
// all producers declared within this class have the highest priority in this test's deployment
public class LocallyEnabledAlternative {

    @Produces
    @Alternative
    @Priority(100)
    public Foo produceAltFoo() {
        return new Foo(ProducerOnLocallyEnabledAltTest.ALT2);
    }

    @Produces
    // @Alternative deliberately left out
    @Priority(100)
    public Bar produceAltBar() {
        return new Bar(ProducerOnLocallyEnabledAltTest.ALT2);
    }
}
