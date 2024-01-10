package org.jboss.weld.tests.invokable.lookup.unsatisfied;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InvokableBean {

    // there is no String bean
    public String unsatisfiedLookup(String a) {
        return a;
    }
}
