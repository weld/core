package org.jboss.weld.tests.contexts.creational;

import jakarta.enterprise.context.Dependent;

@Dependent
@PreDestroyBinding
public class BeanWithPreDestroyInterceptor {

    public void ping() {
    }
}
