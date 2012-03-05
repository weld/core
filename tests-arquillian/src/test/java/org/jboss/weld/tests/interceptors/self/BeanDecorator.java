package org.jboss.weld.tests.interceptors.self;

import java.util.ArrayList;
import java.util.List;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

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
       decoratedInvocationCount ++;
       delegate.doDecorated();
    }
}
