package org.jboss.weld.lite.extension.translator.util.reflection;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

final class ParameterizedTypeImpl implements java.lang.reflect.ParameterizedType {
    private final Class<?> genericClass;
    private final java.lang.reflect.Type[] typeArguments;
    private final java.lang.reflect.Type ownerType;

    ParameterizedTypeImpl(Class<?> genericClass, java.lang.reflect.Type... typeArguments) {
        this(genericClass, typeArguments, null);
    }

    ParameterizedTypeImpl(Class<?> genericClass, java.lang.reflect.Type[] typeArguments, java.lang.reflect.Type ownerType) {
        this.genericClass = genericClass;
        this.typeArguments = typeArguments;
        this.ownerType = ownerType;
    }

    public java.lang.reflect.Type getRawType() {
        return genericClass;
    }

    public java.lang.reflect.Type[] getActualTypeArguments() {
        return typeArguments;
    }

    public java.lang.reflect.Type getOwnerType() {
        return ownerType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.ParameterizedType that = (java.lang.reflect.ParameterizedType) o;
            return Objects.equals(ownerType, that.getOwnerType())
                    && Objects.equals(genericClass, that.getRawType())
                    && Arrays.equals(typeArguments, that.getActualTypeArguments());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(typeArguments) ^ Objects.hashCode(ownerType) ^ Objects.hashCode(genericClass);
    }

    @Override
    public String toString() {
        StringJoiner result = new StringJoiner(",", genericClass.getName() + "<", ">");
        result.setEmptyValue(genericClass.getName());
        for (java.lang.reflect.Type typeArgument : typeArguments) {
            result.add(typeArgument.toString());
        }
        return result.toString();
    }
}
