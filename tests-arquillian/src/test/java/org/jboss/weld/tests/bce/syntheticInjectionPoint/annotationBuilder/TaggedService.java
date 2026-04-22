package org.jboss.weld.tests.bce.syntheticInjectionPoint.annotationBuilder;

import jakarta.enterprise.context.Dependent;

@Dependent
@Tagged("foo")
public class TaggedService implements Service {
    @Override
    public String name() {
        return "tagged-foo";
    }
}
