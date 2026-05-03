package org.jboss.weld.tests.autoclose.basic;

import jakarta.enterprise.context.AutoClose;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

import org.jboss.weld.test.util.ActionSequence;

@Dependent
public class ResourceProducer {

    @Produces
    @Dependent
    @AutoClose
    @ProducerMethodQualifier
    public CloseableResource produceMethod() {
        return new CloseableResource("producerMethod");
    }

    @Produces
    @Dependent
    @AutoClose
    @ProducerFieldQualifier
    public CloseableResource producerField = new CloseableResource("producerField");

    @Produces
    @Dependent
    @AutoClose
    @WithDisposerQualifier
    public CloseableResource produceWithDisposer() {
        return new CloseableResource("withDisposer");
    }

    public void disposeWithDisposer(@Disposes @WithDisposerQualifier CloseableResource resource) {
        ActionSequence.addAction("disposer");
    }
}
