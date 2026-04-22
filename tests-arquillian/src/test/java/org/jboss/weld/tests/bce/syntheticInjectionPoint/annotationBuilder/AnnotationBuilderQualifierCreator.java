package org.jboss.weld.tests.bce.syntheticInjectionPoint.annotationBuilder;

import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticInjections;

public class AnnotationBuilderQualifierCreator implements SyntheticBeanCreator<SyntheticPojo> {
    @Override
    public SyntheticPojo create(SyntheticInjections injections, Parameters params) {
        Service plain = injections.get(Service.class);
        Service special = injections.get(Service.class, Special.Literal.INSTANCE);
        Service tagged = injections.get(Service.class, Tagged.Literal.of("foo"));
        return new SyntheticPojo(plain.name(), special.name(), tagged.name());
    }
}
