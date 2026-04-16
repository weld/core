package org.jboss.weld.tests.bce.syntheticInjectionPoint.basic;

import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticInjections;

/**
 * Uses create(SyntheticInjections, Parameters) with
 * withInjectionPoint(Class, Annotation...) — no qualifier (defaults to @Default).
 */
public class NewApiClassAnnotationCreator implements SyntheticBeanCreator<SyntheticResult> {
    @Override
    public SyntheticResult create(SyntheticInjections injections, Parameters params) {
        Alpha alpha = injections.get(Alpha.class);
        return new SyntheticResult("newClassAnnotation:" + alpha.ping());
    }
}
