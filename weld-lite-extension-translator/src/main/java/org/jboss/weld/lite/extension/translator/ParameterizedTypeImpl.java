package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.types.ClassType;
import jakarta.enterprise.lang.model.types.ParameterizedType;
import jakarta.enterprise.lang.model.types.Type;
import org.jboss.weld.lite.extension.translator.util.AnnotationOverrides;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class ParameterizedTypeImpl extends TypeImpl<java.lang.reflect.AnnotatedParameterizedType> implements ParameterizedType {
    ParameterizedTypeImpl(java.lang.reflect.AnnotatedParameterizedType reflectionType, BeanManager bm) {
        this(reflectionType, null, bm);
    }

    ParameterizedTypeImpl(java.lang.reflect.AnnotatedParameterizedType reflectionType, AnnotationOverrides overrides, BeanManager bm) {
        super(reflectionType, overrides, bm);
    }

    @Override
    public ClassType genericClass() {
        java.lang.reflect.ParameterizedType type = (java.lang.reflect.ParameterizedType) reflection.getType();
        return new ClassTypeImpl((Class<?>) type.getRawType(), null, bm);
    }

    @Override
    public List<Type> typeArguments() {
        return Arrays.stream(reflection.getAnnotatedActualTypeArguments())
                .map(annotatedType -> TypeImpl.fromReflectionType(annotatedType, bm))
                .collect(Collectors.toList());
    }
}
