package org.jboss.weld.tests.bce.syntheticInjectionPoint.basic;

import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticInjections;

/**
 * Uses create(SyntheticInjections, Parameters) with
 * withInjectionPoint(Type, Annotation...) — single qualifier.
 */
public class NewApiTypeAnnotationCreator implements SyntheticBeanCreator<SyntheticResult> {
    @Override
    public SyntheticResult create(SyntheticInjections injections, Parameters params) {
        Bravo bravo = injections.get(Bravo.class, MyQualifier.Literal.INSTANCE);
        return new SyntheticResult("newTypeAnnotation:" + bravo.ping());
    }
}
