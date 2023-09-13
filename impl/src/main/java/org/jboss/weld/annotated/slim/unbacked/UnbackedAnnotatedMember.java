package org.jboss.weld.annotated.slim.unbacked;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedMember;

import org.jboss.weld.util.reflection.Reflections;

public abstract class UnbackedAnnotatedMember<X> extends UnbackedAnnotated implements AnnotatedMember<X> {

    public UnbackedAnnotatedMember(Type baseType, Set<Type> typeClosure, Set<Annotation> annotations,
            UnbackedAnnotatedType<X> declaringType) {
        super(baseType, typeClosure, annotations);
        this.declaringType = declaringType;
    }

    private UnbackedAnnotatedType<X> declaringType;

    public boolean isStatic() {
        return Reflections.isStatic(getJavaMember());
    }

    public UnbackedAnnotatedType<X> getDeclaringType() {
        return declaringType;
    }

}
