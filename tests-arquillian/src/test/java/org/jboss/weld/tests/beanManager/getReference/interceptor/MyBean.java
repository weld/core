package org.jboss.weld.tests.beanManager.getReference.interceptor;

import jakarta.enterprise.context.Dependent;

@Dependent
@MyBinding
public class MyBean {

    public void ping() {

    }
}
