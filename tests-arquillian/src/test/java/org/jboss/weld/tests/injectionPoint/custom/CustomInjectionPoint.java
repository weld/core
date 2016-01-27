package org.jboss.weld.tests.injectionPoint.custom;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.literal.DefaultLiteral;

public class CustomInjectionPoint implements InjectionPoint {

    private final Bean<?> bean;
    private final AnnotatedField<?> annotatedField;
    private final Type type;


    public CustomInjectionPoint(Bean<?> bean, AnnotatedField<?> annotatedField, Type type) {
        this.bean = bean;
        this.annotatedField = annotatedField;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Set<Annotation> getQualifiers() {
        return Collections.<Annotation>singleton(DefaultLiteral.INSTANCE);
    }

    public Bean<?> getBean() {
        return bean;
    }

    public Member getMember() {
        return annotatedField.getJavaMember();
    }

    public Annotated getAnnotated() {
        return annotatedField;
    }

    public boolean isDelegate() {
        return false;
    }

    public boolean isTransient() {
        return false;
    }
}