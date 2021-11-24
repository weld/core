package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.lang.model.types.Type;
import jakarta.enterprise.lang.model.types.TypeVariable;
import org.jboss.weld.lite.extension.translator.util.AnnotationOverrides;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class TypeVariableImpl extends TypeImpl<java.lang.reflect.AnnotatedTypeVariable> implements TypeVariable {
    TypeVariableImpl(java.lang.reflect.AnnotatedTypeVariable reflectionType) {
        this(reflectionType, null);
    }

    TypeVariableImpl(java.lang.reflect.AnnotatedTypeVariable reflectionType, AnnotationOverrides overrides) {
        super(reflectionType, overrides);
    }

    @Override
    public String name() {
        return reflection.getType().getTypeName();
    }

    @Override
    public List<Type> bounds() {
        return Arrays.stream(reflection.getAnnotatedBounds())
                .map(TypeImpl::fromReflectionType)
                .collect(Collectors.toList());
    }
}
