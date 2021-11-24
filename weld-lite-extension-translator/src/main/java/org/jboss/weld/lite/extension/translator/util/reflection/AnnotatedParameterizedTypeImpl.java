package org.jboss.weld.lite.extension.translator.util.reflection;

import java.util.Arrays;

final class AnnotatedParameterizedTypeImpl extends AbstractEmptyAnnotatedType implements java.lang.reflect.AnnotatedParameterizedType {
    private final java.lang.reflect.ParameterizedType parameterizedType;

    AnnotatedParameterizedTypeImpl(java.lang.reflect.ParameterizedType parameterizedType) {
        this.parameterizedType = parameterizedType;
    }

    @Override
    public java.lang.reflect.AnnotatedType[] getAnnotatedActualTypeArguments() {
        return Arrays.stream(parameterizedType.getActualTypeArguments())
                .map(AnnotatedTypes::from)
                .toArray(java.lang.reflect.AnnotatedType[]::new);
    }

    // added in Java 9
    /*
    @Override
    */
    public java.lang.reflect.AnnotatedType getAnnotatedOwnerType() {
        java.lang.reflect.Type ownerType = parameterizedType.getOwnerType();
        return ownerType == null ? null : AnnotatedTypes.from(ownerType);
    }

    @Override
    public java.lang.reflect.Type getType() {
        return parameterizedType;
    }

    @Override
    public String toString() {
        return parameterizedType.toString();
    }
}
