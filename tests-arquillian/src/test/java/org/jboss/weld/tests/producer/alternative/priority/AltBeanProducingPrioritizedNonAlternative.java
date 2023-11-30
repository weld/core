package org.jboss.weld.tests.producer.alternative.priority;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
@Alternative
@Priority(1)
public class AltBeanProducingPrioritizedNonAlternative {

    @Priority(20) // should override class-level priority value and hence end up having the highest priority
    @Produces
    @ProducedByMethod
    Delta producer1() {
        return new Delta(ProducerExplicitPriorityTest.ALT2);
    }

    @Priority(20) // should override class-level priority value and hence end up having the highest priority
    @Produces
    @ProducedByField
    Delta producer2 = new Delta(ProducerExplicitPriorityTest.ALT2);
}
