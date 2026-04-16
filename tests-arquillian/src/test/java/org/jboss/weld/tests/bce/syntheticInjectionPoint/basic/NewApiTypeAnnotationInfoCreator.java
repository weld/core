package org.jboss.weld.tests.bce.syntheticInjectionPoint.basic;

import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticInjections;

/**
 * Uses create(SyntheticInjections, Parameters) with
 * withInjectionPoint(Type, AnnotationInfo...) — two qualifiers.
 */
public class NewApiTypeAnnotationInfoCreator implements SyntheticBeanCreator<SyntheticResult> {
    @Override
    public SyntheticResult create(SyntheticInjections injections, Parameters params) {
        Charlie charlie = injections.get(Charlie.class, MyQualifier.Literal.INSTANCE, AnotherQualifier.Literal.INSTANCE);
        return new SyntheticResult("newTypeAnnotationInfo:" + charlie.ping());
    }
}
