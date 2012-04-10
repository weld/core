package org.jboss.weld.annotated.slim.backed;

import java.lang.reflect.Type;

import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.util.reflection.Reflections;

public abstract class BackedAnnotatedMember<X> extends BackedAnnotated implements AnnotatedMember<X> {

    private BackedAnnotatedType<X> declaringType;

    public BackedAnnotatedMember(Type baseType, BackedAnnotatedType<X> declaringType) {
        super(baseType);
        this.declaringType = declaringType;
    }

    public boolean isStatic() {
        return Reflections.isStatic(getJavaMember());
    }

    public AnnotatedType<X> getDeclaringType() {
        return declaringType;
    }
}
