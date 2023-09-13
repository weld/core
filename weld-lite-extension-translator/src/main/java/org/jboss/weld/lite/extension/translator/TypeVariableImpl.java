package org.jboss.weld.lite.extension.translator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.types.Type;
import jakarta.enterprise.lang.model.types.TypeVariable;

import org.jboss.weld.lite.extension.translator.util.AnnotationOverrides;

class TypeVariableImpl extends TypeImpl<java.lang.reflect.AnnotatedTypeVariable> implements TypeVariable {
    TypeVariableImpl(java.lang.reflect.AnnotatedTypeVariable reflectionType, BeanManager bm) {
        this(reflectionType, null, bm);
    }

    TypeVariableImpl(java.lang.reflect.AnnotatedTypeVariable reflectionType, AnnotationOverrides overrides,
            BeanManager bm) {
        super(reflectionType, overrides, bm);
    }

    @Override
    public String name() {
        return reflection.getType().getTypeName();
    }

    @Override
    public List<Type> bounds() {
        return Arrays.stream(reflection.getAnnotatedBounds())
                .map(annotatedType -> TypeImpl.fromReflectionType(annotatedType, bm))
                .collect(Collectors.toList());
    }
}
