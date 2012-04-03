package org.jboss.weld.annotated.slim.unbacked;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.util.reflection.Reflections;

public abstract class UnbackedAnnotatedMember<X> extends UnbackedAnnotated implements AnnotatedMember<X> {

    public UnbackedAnnotatedMember(Type baseType, Set<Type> typeClosure, Set<Annotation> annotations, AnnotatedType<X> declaringType) {
        super(baseType, typeClosure, annotations);
        this.declaringType = declaringType;
    }

    private AnnotatedType<X> declaringType;

    public boolean isStatic() {
        return Reflections.isStatic(getJavaMember());
    }

    public AnnotatedType<X> getDeclaringType() {
        return declaringType;
    }

}
