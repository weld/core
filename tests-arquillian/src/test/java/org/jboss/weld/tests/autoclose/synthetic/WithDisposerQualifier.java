package org.jboss.weld.tests.autoclose.synthetic;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

@Qualifier
@Retention(RUNTIME)
public @interface WithDisposerQualifier {
    class Literal extends AnnotationLiteral<WithDisposerQualifier> implements WithDisposerQualifier {
        public static final Literal INSTANCE = new Literal();
    }
}
