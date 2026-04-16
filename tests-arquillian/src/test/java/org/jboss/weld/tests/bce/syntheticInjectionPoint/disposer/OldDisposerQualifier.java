package org.jboss.weld.tests.bce.syntheticInjectionPoint.disposer;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

@Qualifier
@Retention(RUNTIME)
public @interface OldDisposerQualifier {
    class Literal extends AnnotationLiteral<OldDisposerQualifier> implements OldDisposerQualifier {
        public static final Literal INSTANCE = new Literal();
    }
}
