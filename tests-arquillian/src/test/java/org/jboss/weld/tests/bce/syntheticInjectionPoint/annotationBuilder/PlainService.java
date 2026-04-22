package org.jboss.weld.tests.bce.syntheticInjectionPoint.annotationBuilder;

import jakarta.enterprise.context.Dependent;

@Dependent
public class PlainService implements Service {
    @Override
    public String name() {
        return "plain";
    }
}
