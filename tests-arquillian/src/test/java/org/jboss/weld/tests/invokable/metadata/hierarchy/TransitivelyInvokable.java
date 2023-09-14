package org.jboss.weld.tests.invokable.metadata.hierarchy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.enterprise.invoke.Invokable;
import jakarta.enterprise.util.AnnotationLiteral;

@Invokable
@Retention(RetentionPolicy.RUNTIME)
public @interface TransitivelyInvokable {

    class Literal extends AnnotationLiteral<TransitivelyInvokable> implements TransitivelyInvokable {

        public static final TransitivelyInvokable INSTANCE = new Literal();

        private Literal() {
        }
    }
}
