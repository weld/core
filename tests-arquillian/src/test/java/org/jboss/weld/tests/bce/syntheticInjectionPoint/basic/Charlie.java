package org.jboss.weld.tests.bce.syntheticInjectionPoint.basic;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@MyQualifier
@AnotherQualifier
public class Charlie {
    public String ping() {
        return "charlie";
    }
}
