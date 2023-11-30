package org.jboss.weld.tests.producer.alternative.priority;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class NonAltBeanProducingAlternative {

    @Alternative
    @Priority(10)
    @Produces
    @ProducedByMethod
    Alpha producer1() {
        return new Alpha(ProducerExplicitPriorityTest.ALT);
    }

    @Alternative
    @Priority(10)
    @Produces
    @ProducedByMethod
    Beta producer2() {
        return new Beta(ProducerExplicitPriorityTest.ALT);
    }

    @Alternative
    @Priority(10)
    @Produces
    @ProducedByField
    Alpha producer3 = new Alpha(ProducerExplicitPriorityTest.ALT);

    @Alternative
    @Priority(10)
    @Produces
    @ProducedByField
    Beta producer4 = new Beta(ProducerExplicitPriorityTest.ALT);

    @Produces
    @ProducedByMethod
    @Alternative
    @Priority(10)
    Gamma producer5() {
        return new Gamma(ProducerExplicitPriorityTest.ALT);
    }

    @Produces
    @ProducedByField
    @Alternative
    @Priority(10)
    Gamma producer6 = new Gamma(ProducerExplicitPriorityTest.ALT);

    @Produces
    @ProducedByMethod
    @Alternative
    @Priority(10)
    Delta producer7() {
        return new Delta(ProducerExplicitPriorityTest.ALT);
    }

    @Produces
    @ProducedByField
    @Alternative
    @Priority(10)
    Delta producer8 = new Delta(ProducerExplicitPriorityTest.ALT);

}
