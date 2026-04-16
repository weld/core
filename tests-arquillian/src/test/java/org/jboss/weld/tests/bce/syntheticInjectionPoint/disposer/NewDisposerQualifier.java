package org.jboss.weld.tests.bce.syntheticInjectionPoint.disposer;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

@Qualifier
@Retention(RUNTIME)
public @interface NewDisposerQualifier {
    class Literal extends AnnotationLiteral<NewDisposerQualifier> implements NewDisposerQualifier {
        public static final Literal INSTANCE = new Literal();
    }
}
