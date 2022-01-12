package org.jboss.weld.lite.extension.translator.util.reflection;

import org.jboss.weld.lite.extension.translator.logging.LiteExtensionTranslatorLogger;

public final class AnnotatedTypes {

    private AnnotatedTypes() {
    }

    public static java.lang.reflect.AnnotatedType from(java.lang.reflect.Type type) {
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            if (clazz.isArray()) {
                int dimensions = 0;
                while (clazz.isArray()) {
                    dimensions++;
                    clazz = clazz.getComponentType();
                }
                return array(clazz, dimensions);
            }
            return new AnnotatedTypeImpl(clazz);
        } else if (type instanceof java.lang.reflect.ParameterizedType) {
            return new AnnotatedParameterizedTypeImpl((java.lang.reflect.ParameterizedType) type);
        } else if (type instanceof java.lang.reflect.TypeVariable) {
            return new AnnotatedTypeVariableImpl((java.lang.reflect.TypeVariable<?>) type);
        } else if (type instanceof java.lang.reflect.WildcardType) {
            return new AnnotatedWildcardTypeImpl((java.lang.reflect.WildcardType) type);
        } else if (type instanceof java.lang.reflect.GenericArrayType) {
            return new AnnotatedArrayTypeImpl((java.lang.reflect.GenericArrayType) type);
        } else {
            throw LiteExtensionTranslatorLogger.LOG.unknownReflectionType(type);
        }
    }

    public static java.lang.reflect.AnnotatedArrayType array(java.lang.reflect.Type componentType, int dimensions) {
        java.lang.reflect.GenericArrayType type = new GenericArrayTypeImpl(componentType);
        for (int i = 0; i < dimensions - 1; i++) {
            type = new GenericArrayTypeImpl(type);
        }
        return new AnnotatedArrayTypeImpl(type);
    }

    public static java.lang.reflect.AnnotatedParameterizedType parameterized(Class<?> genericClass,
            java.lang.reflect.Type... typeArguments) {
        return new AnnotatedParameterizedTypeImpl(new ParameterizedTypeImpl(genericClass, typeArguments));
    }

    public static java.lang.reflect.AnnotatedTypeVariable typeVariable(java.lang.reflect.TypeVariable<?> typeVariable) {
        return new AnnotatedTypeVariableImpl(typeVariable);
    }

    public static java.lang.reflect.AnnotatedWildcardType wildcardWithUpperBound(java.lang.reflect.Type upperBound) {
        return new AnnotatedWildcardTypeImpl(WildcardTypeImpl.withUpperBound(upperBound));
    }

    public static java.lang.reflect.AnnotatedWildcardType wildcardWithLowerBound(java.lang.reflect.Type lowerBound) {
        return new AnnotatedWildcardTypeImpl(WildcardTypeImpl.withLowerBound(lowerBound));
    }

    public static java.lang.reflect.AnnotatedWildcardType unboundedWildcardType() {
        return new AnnotatedWildcardTypeImpl(WildcardTypeImpl.unbounded());
    }
}
