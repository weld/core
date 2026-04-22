package org.jboss.weld.tests.bce.syntheticInjectionPoint.annotationBuilder;

import jakarta.enterprise.context.Dependent;

@Dependent
@Special
public class SpecialService implements Service {
    @Override
    public String name() {
        return "special";
    }
}
