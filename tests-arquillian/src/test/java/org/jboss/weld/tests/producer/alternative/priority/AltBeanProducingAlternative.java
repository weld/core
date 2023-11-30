package org.jboss.weld.tests.producer.alternative.priority;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;

@Alternative
@Priority(1)
@ApplicationScoped
public class AltBeanProducingAlternative {

    @Alternative
    @Priority(20) // should override class-level priority value and hence end up having the highest priority
    @Produces
    @ProducedByMethod
    Beta producer1() {
        return new Beta(ProducerExplicitPriorityTest.ALT2);
    }

    @Alternative
    @Priority(20) // should override class-level priority value and hence end up having the highest priority
    @Produces
    @ProducedByField
    Beta producer2 = new Beta(ProducerExplicitPriorityTest.ALT2);
}
