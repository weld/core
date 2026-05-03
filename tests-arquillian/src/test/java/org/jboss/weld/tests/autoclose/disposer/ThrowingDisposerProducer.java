package org.jboss.weld.tests.autoclose.disposer;

import jakarta.enterprise.context.AutoClose;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

import org.jboss.weld.test.util.ActionSequence;

@Dependent
public class ThrowingDisposerProducer {

    @Produces
    @Dependent
    @AutoClose
    @ThrowingDisposerQualifier
    public CloseableResource produceWithThrowingDisposer() {
        return new CloseableResource("throwingDisposer");
    }

    public void dispose(@Disposes @ThrowingDisposerQualifier CloseableResource resource) {
        ActionSequence.addAction("disposer.throwing");
        throw new RuntimeException("disposer failed");
    }
}
