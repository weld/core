package org.jboss.weld.tests.invokable.lookup.ambiguous;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InvokableBean {

    // there are two producers providing a bean for the first argument
    public String ambiguousLookup(@MyQualifier5 String a) {
        return a;
    }
}
