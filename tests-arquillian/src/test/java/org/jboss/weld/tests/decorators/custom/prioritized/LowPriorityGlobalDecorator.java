package org.jboss.weld.tests.decorators.custom.prioritized;

import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;

import org.jboss.weld.test.util.ActionSequence;

@Priority(500)
@Decorator
public class LowPriorityGlobalDecorator implements Decorated {

    @Inject
    @Delegate
    Decorated delegate;

    @Override
    public int foo() {
        ActionSequence.addAction(LowPriorityGlobalDecorator.class.getName());
        return delegate.foo();
    }

}
