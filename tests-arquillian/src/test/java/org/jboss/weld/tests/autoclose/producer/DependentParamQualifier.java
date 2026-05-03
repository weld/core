package org.jboss.weld.tests.autoclose.producer;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

@Qualifier
@Retention(RUNTIME)
public @interface DependentParamQualifier {
    class Literal extends AnnotationLiteral<DependentParamQualifier> implements DependentParamQualifier {
        public static final Literal INSTANCE = new Literal();
    }
}
