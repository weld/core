package org.jboss.weld.tests.producer.alternative.priority;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

// produces standard (non-alternative) beans that should be replaced by alternatives
@ApplicationScoped
public class RegularBeanProducer {

    @Produces
    @ProducedByMethod
    Alpha producer1() {
        return new Alpha(ProducerExplicitPriorityTest.DEFAULT);
    }

    @Produces
    @ProducedByMethod
    Beta producer2() {
        return new Beta(ProducerExplicitPriorityTest.DEFAULT);
    }

    @Produces
    @ProducedByField
    Alpha producer3 = new Alpha(ProducerExplicitPriorityTest.DEFAULT);

    @Produces
    @ProducedByField
    Beta producer4 = new Beta(ProducerExplicitPriorityTest.DEFAULT);

    @Produces
    @ProducedByMethod
    Gamma producer5() {
        return new Gamma(ProducerExplicitPriorityTest.DEFAULT);
    }

    @Produces
    @ProducedByField
    Gamma producer6 = new Gamma(ProducerExplicitPriorityTest.DEFAULT);

    @Produces
    @ProducedByMethod
    Delta producer7() {
        return new Delta(ProducerExplicitPriorityTest.DEFAULT);
    }

    @Produces
    @ProducedByField
    Delta producer8 = new Delta(ProducerExplicitPriorityTest.DEFAULT);
}
