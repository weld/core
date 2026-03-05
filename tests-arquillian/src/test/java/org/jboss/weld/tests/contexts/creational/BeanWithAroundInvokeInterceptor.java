package org.jboss.weld.tests.contexts.creational;

import jakarta.enterprise.context.Dependent;

@AroundInvokeBinding
@Dependent
public class BeanWithAroundInvokeInterceptor {

    public void ping() {

    }
}
