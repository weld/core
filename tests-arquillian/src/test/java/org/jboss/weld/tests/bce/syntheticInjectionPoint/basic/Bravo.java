package org.jboss.weld.tests.bce.syntheticInjectionPoint.basic;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@MyQualifier
public class Bravo {
    public String ping() {
        return "bravo";
    }
}
