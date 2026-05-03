package org.jboss.weld.tests.bce.syntheticInjectionPoint.annotationBuilder;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilder;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;

public class AnnotationBuilderQualifierExtension implements BuildCompatibleExtension {
    @Synthesis
    public void synthesize(SyntheticComponents syn) {
        syn.addBean(SyntheticPojo.class)
                .type(SyntheticPojo.class)
                .scope(Dependent.class)
                .withInjectionPoint(Service.class)
                .withInjectionPoint(Service.class, AnnotationBuilder.of(Special.class).build())
                .withInjectionPoint(Service.class,
                        AnnotationBuilder.of(Tagged.class).member("value", "foo").build())
                .createWith(AnnotationBuilderQualifierCreator.class);
    }
}
