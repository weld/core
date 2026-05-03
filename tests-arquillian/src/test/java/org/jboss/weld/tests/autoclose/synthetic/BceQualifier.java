package org.jboss.weld.tests.autoclose.synthetic;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

@Qualifier
@Retention(RUNTIME)
public @interface BceQualifier {
    class Literal extends AnnotationLiteral<BceQualifier> implements BceQualifier {
        public static final Literal INSTANCE = new Literal();
    }
}
