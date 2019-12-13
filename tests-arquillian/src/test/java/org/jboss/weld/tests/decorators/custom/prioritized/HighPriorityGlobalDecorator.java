package org.jboss.weld.tests.decorators.custom.prioritized;

import javax.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;

import org.jboss.weld.test.util.ActionSequence;

@Priority(1500)
@Decorator
public class HighPriorityGlobalDecorator implements Decorated {

    @Inject
    @Delegate
    Decorated delegate;

    @Override
    public int foo() {
        ActionSequence.addAction(HighPriorityGlobalDecorator.class.getName());
        return delegate.foo();
    }

}
