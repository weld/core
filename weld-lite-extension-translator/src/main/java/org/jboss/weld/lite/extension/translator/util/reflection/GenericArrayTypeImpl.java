package org.jboss.weld.lite.extension.translator.util.reflection;

import java.util.Objects;

final class GenericArrayTypeImpl implements java.lang.reflect.GenericArrayType {
    private final java.lang.reflect.Type componentType;

    GenericArrayTypeImpl(java.lang.reflect.Type componentType) {
        this.componentType = componentType;
    }

    @Override
    public java.lang.reflect.Type getGenericComponentType() {
        return componentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof java.lang.reflect.GenericArrayType) {
            java.lang.reflect.GenericArrayType that = (java.lang.reflect.GenericArrayType) o;
            return Objects.equals(componentType, that.getGenericComponentType());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(componentType);
    }

    @Override
    public String toString() {
        if (componentType instanceof Class) {
            return ((Class<?>) componentType).getName() + "[]";
        }

        return componentType + "[]";
    }
}
