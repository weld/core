package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.types.ClassType;

import org.jboss.weld.lite.extension.translator.util.AnnotationOverrides;
import org.jboss.weld.lite.extension.translator.util.reflection.AnnotatedTypes;

class ClassTypeImpl extends TypeImpl<java.lang.reflect.AnnotatedType> implements ClassType {
    final Class<?> clazz;

    ClassTypeImpl(java.lang.reflect.AnnotatedType clazz, BeanManager bm) {
        this(clazz, null, bm);
    }

    ClassTypeImpl(java.lang.reflect.AnnotatedType clazz, AnnotationOverrides overrides, BeanManager bm) {
        super(clazz, overrides, bm);
        this.clazz = (Class<?>) clazz.getType();
    }

    ClassTypeImpl(Class<?> clazz, BeanManager bm) {
        this(clazz, null, bm);
    }

    ClassTypeImpl(Class<?> clazz, AnnotationOverrides overrides, BeanManager bm) {
        super(AnnotatedTypes.from(clazz), overrides, bm);
        this.clazz = clazz;
    }

    @Override
    public ClassInfo declaration() {
        return new ClassInfoImpl(bm.createAnnotatedType(clazz), bm);
    }
}
