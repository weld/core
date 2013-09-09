package org.jboss.weld.tests.annotatedType.interceptors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

@Qualifier
@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Additional {

    @SuppressWarnings("all")
    public static class Literal extends AnnotationLiteral<Additional> implements Additional {

        public static final Literal INSTANCE = new Literal();

    }
}
