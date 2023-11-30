package org.jboss.weld.tests.producer.alternative.priority;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
@Priority(500)
public class NonAltBeanWithPrioProducingAlternative {

    @Produces
    @ProducedByMethod
    @Alternative
    Gamma producer5() {
        return new Gamma(ProducerExplicitPriorityTest.ALT2);
    }

    @Produces
    @ProducedByField
    @Alternative
    Gamma producer6 = new Gamma(ProducerExplicitPriorityTest.ALT2);
}
