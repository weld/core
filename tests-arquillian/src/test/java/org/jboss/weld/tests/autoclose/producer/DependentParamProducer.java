package org.jboss.weld.tests.autoclose.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.AutoClose;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

import org.jboss.weld.test.util.ActionSequence;

@ApplicationScoped
public class DependentParamProducer {

    @Produces
    @Dependent
    @AutoClose
    @DependentParamQualifier
    public CloseableResource produce(DependentHelper helper) {
        return new CloseableResource("withDependentParam");
    }

    public void dispose(@Disposes @DependentParamQualifier CloseableResource resource) {
        ActionSequence.addAction("disposer");
    }
}
