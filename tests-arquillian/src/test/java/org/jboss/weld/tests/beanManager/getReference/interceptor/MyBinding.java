package org.jboss.weld.tests.beanManager.getReference.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.interceptor.InterceptorBinding;

@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
public @interface MyBinding {

    public static class Literal extends AnnotationLiteral<MyBinding> implements MyBinding {

        public static final Literal INSTANCE = new Literal();

    }
}
