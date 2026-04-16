package org.jboss.weld.tests.bce.syntheticInjectionPoint.basic;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Registration;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.inject.build.compatible.spi.Types;
import jakarta.enterprise.lang.model.AnnotationInfo;

public class BasicSyntheticInjectionPointExtension implements BuildCompatibleExtension {

    // Captured during @Registration for use in @Synthesis with AnnotationInfo overloads
    private final List<AnnotationInfo> charlieQualifiers = new ArrayList<>();

    @Registration(types = Charlie.class)
    public void captureCharlieQualifiers(BeanInfo bean) {
        // Capture @MyQualifier and @AnotherQualifier as AnnotationInfo from the bean metadata
        for (AnnotationInfo qualifier : bean.qualifiers()) {
            String name = qualifier.declaration().name();
            if (name.endsWith("MyQualifier") || name.endsWith("AnotherQualifier")) {
                charlieQualifiers.add(qualifier);
            }
        }
    }

    @Synthesis
    public void register(SyntheticComponents syn, Types types) {
        // Old API, no withInjectionPoint
        syn.addBean(SyntheticResult.class)
                .type(SyntheticResult.class)
                .qualifier(OldApiQualifier.class)
                .scope(ApplicationScoped.class)
                .createWith(OldApiCreator.class);

        // withInjectionPoint(Class, Annotation...) — @Default Alpha
        syn.addBean(SyntheticResult.class)
                .type(SyntheticResult.class)
                .qualifier(Scenario2aQualifier.class)
                .scope(ApplicationScoped.class)
                .withInjectionPoint(Alpha.class, new Annotation[0])
                .createWith(NewApiClassAnnotationCreator.class);

        // withInjectionPoint(Class, AnnotationInfo...) — 2 qualifiers
        // Uses AnnotationInfo captured from Charlie's bean metadata during @Registration
        syn.addBean(SyntheticResult.class)
                .type(SyntheticResult.class)
                .qualifier(Scenario2bQualifier.class)
                .scope(ApplicationScoped.class)
                .withInjectionPoint(Charlie.class,
                        charlieQualifiers.toArray(new AnnotationInfo[0]))
                .createWith(NewApiClassAnnotationInfoCreator.class);

        // withInjectionPoint(Type, Annotation...) — single qualifier
        syn.addBean(SyntheticResult.class)
                .type(SyntheticResult.class)
                .qualifier(Scenario3aQualifier.class)
                .scope(ApplicationScoped.class)
                .withInjectionPoint(types.of(Bravo.class), MyQualifier.Literal.INSTANCE)
                .createWith(NewApiTypeAnnotationCreator.class);

        // withInjectionPoint(Type, AnnotationInfo...) — 2 qualifiers
        // Uses AnnotationInfo captured from Charlie's bean metadata during @Registration
        syn.addBean(SyntheticResult.class)
                .type(SyntheticResult.class)
                .qualifier(Scenario3bQualifier.class)
                .scope(ApplicationScoped.class)
                .withInjectionPoint(types.of(Charlie.class),
                        charlieQualifiers.toArray(new AnnotationInfo[0]))
                .createWith(NewApiTypeAnnotationInfoCreator.class);

        // @Dependent synthetic bean that looks up a @Dependent helper via SyntheticInjections.
        // When this bean is destroyed, the dependent helper should also be destroyed.
        syn.addBean(SyntheticResult.class)
                .type(SyntheticResult.class)
                .qualifier(DependentCleanupQualifier.class)
                .scope(jakarta.enterprise.context.Dependent.class)
                .withInjectionPoint(DependentHelper.class, new Annotation[0])
                .createWith(DependentCleanupCreator.class);
    }
}
