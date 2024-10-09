package org.jboss.weld.tests.proxy.sealed;

import jakarta.enterprise.context.Dependent;

@Dependent
@MyBinding
public sealed class MyDependentIntercepted permits MyDependentInterceptedSubclass {

    public String ping() {
        return MyDependentIntercepted.class.getSimpleName();
    }
}
