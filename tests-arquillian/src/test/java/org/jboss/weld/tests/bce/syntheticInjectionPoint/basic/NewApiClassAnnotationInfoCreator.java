package org.jboss.weld.tests.bce.syntheticInjectionPoint.basic;

import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticInjections;

/**
 * Uses create(SyntheticInjections, Parameters) with
 * withInjectionPoint(Class, AnnotationInfo...) — two qualifiers.
 */
public class NewApiClassAnnotationInfoCreator implements SyntheticBeanCreator<SyntheticResult> {
    @Override
    public SyntheticResult create(SyntheticInjections injections, Parameters params) {
        Charlie charlie = injections.get(Charlie.class, MyQualifier.Literal.INSTANCE, AnotherQualifier.Literal.INSTANCE);
        return new SyntheticResult("newClassAnnotationInfo:" + charlie.ping());
    }
}
