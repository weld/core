package org.jboss.weld.tests.bce.syntheticInjectionPoint.basic;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Alpha {
    public String ping() {
        return "alpha";
    }
}
