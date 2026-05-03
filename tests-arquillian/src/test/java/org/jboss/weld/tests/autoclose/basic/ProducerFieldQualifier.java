package org.jboss.weld.tests.autoclose.basic;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

@Qualifier
@Retention(RUNTIME)
public @interface ProducerFieldQualifier {
    class Literal extends AnnotationLiteral<ProducerFieldQualifier> implements ProducerFieldQualifier {
        public static final Literal INSTANCE = new Literal();
    }
}
