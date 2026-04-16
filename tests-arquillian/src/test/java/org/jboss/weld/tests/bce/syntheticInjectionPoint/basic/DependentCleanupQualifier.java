package org.jboss.weld.tests.bce.syntheticInjectionPoint.basic;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

@Qualifier
@Retention(RUNTIME)
public @interface DependentCleanupQualifier {
    class Literal extends AnnotationLiteral<DependentCleanupQualifier> implements DependentCleanupQualifier {
        public static final Literal INSTANCE = new Literal();
    }
}
