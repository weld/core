package org.jboss.weld.tests.autoclose.producer;

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
    @AutoCloseProducerQualifier
    public CloseableResource produce() {
        return new CloseableResource("produced");
    }

    public void dispose(@Disposes @AutoCloseProducerQualifier CloseableResource resource) {
        ActionSequence.addAction("disposer");
    }
}
