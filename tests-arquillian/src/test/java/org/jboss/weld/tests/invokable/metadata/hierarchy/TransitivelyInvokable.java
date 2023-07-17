package org.jboss.weld.tests.invokable.metadata.hierarchy;

import jakarta.enterprise.invoke.Invokable;
import jakarta.enterprise.util.AnnotationLiteral;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Invokable
@Retention(RetentionPolicy.RUNTIME)
public @interface TransitivelyInvokable {

    class Literal extends AnnotationLiteral<TransitivelyInvokable> implements TransitivelyInvokable {

        public static final TransitivelyInvokable INSTANCE = new Literal();

        private Literal() {
        }
    }
}
