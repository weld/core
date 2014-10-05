package org.jboss.weld.tests.proxy.weld9999;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

public class Producers {
    @Produces
    @Qualifier1
    @RequestScoped
    public TestComponent produceRequestScopedComponent() {
        return new TestComponent();
    }

    @Produces
    @Qualifier2
    @CustomScoped
    public TestComponent produceCustomScopedComponent() {
        return new TestComponent();
    }
}
