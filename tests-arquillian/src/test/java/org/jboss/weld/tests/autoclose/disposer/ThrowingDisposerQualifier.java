package org.jboss.weld.tests.autoclose.disposer;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

@Qualifier
@Retention(RUNTIME)
public @interface ThrowingDisposerQualifier {
    class Literal extends AnnotationLiteral<ThrowingDisposerQualifier> implements ThrowingDisposerQualifier {
        public static final Literal INSTANCE = new Literal();
    }
}
