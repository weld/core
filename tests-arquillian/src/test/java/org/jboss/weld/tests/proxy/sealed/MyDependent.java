package org.jboss.weld.tests.proxy.sealed;

import jakarta.enterprise.context.Dependent;

@Dependent
public sealed class MyDependent permits MyDependentSubclass {

    public String ping() {
        return MyDependent.class.getSimpleName();
    }
}
