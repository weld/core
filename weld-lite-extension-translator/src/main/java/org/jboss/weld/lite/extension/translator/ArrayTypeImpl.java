package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.types.ArrayType;
import jakarta.enterprise.lang.model.types.Type;

import org.jboss.weld.lite.extension.translator.util.AnnotationOverrides;

class ArrayTypeImpl extends TypeImpl<java.lang.reflect.AnnotatedArrayType> implements ArrayType {
    private final java.lang.reflect.AnnotatedType componentType;

    ArrayTypeImpl(java.lang.reflect.AnnotatedArrayType reflectionType, BeanManager bm) {
        this(reflectionType, null, bm);
    }

    ArrayTypeImpl(java.lang.reflect.AnnotatedArrayType reflectionType, AnnotationOverrides overrides, BeanManager bm) {
        super(reflectionType, overrides, bm);

        this.componentType = reflectionType.getAnnotatedGenericComponentType();
    }

    @Override
    public Type componentType() {
        return TypeImpl.fromReflectionType(componentType, bm);
    }
}
