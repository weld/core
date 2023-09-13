package org.jboss.weld.tests.interceptors.self;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;

/**
 * @author Marius Bogoevici
 */
@Decorator
public class BeanDecorator implements Decorated {

    @Inject
    @Delegate
    Decorated delegate;

    static int decoratedInvocationCount = 0;

    public static void reset() {
        decoratedInvocationCount = 0;
    }

    public void doDecorated() {
        decoratedInvocationCount++;
        delegate.doDecorated();
    }
}
