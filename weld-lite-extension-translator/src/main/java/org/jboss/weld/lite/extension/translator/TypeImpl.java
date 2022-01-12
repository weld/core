package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.lang.model.types.Type;
import org.jboss.weld.lite.extension.translator.logging.LiteExtensionTranslatorLogger;
import org.jboss.weld.lite.extension.translator.util.AnnotationOverrides;
import org.jboss.weld.lite.extension.translator.util.reflection.AnnotatedTypes;

import java.util.Arrays;
import java.util.Objects;

abstract class TypeImpl<ReflectionType extends java.lang.reflect.AnnotatedType> extends AnnotationTargetImpl<ReflectionType> implements Type {
    TypeImpl(ReflectionType reflectionType, AnnotationOverrides overrides) {
        super(reflectionType, overrides);
    }

    static Type fromReflectionType(java.lang.reflect.AnnotatedType reflectionType) {
        return fromReflectionType(reflectionType, null);
    }

    static Type fromReflectionType(java.lang.reflect.AnnotatedType reflectionType, AnnotationOverrides overrides) {
        if (reflectionType instanceof java.lang.reflect.AnnotatedParameterizedType) {
            return new ParameterizedTypeImpl((java.lang.reflect.AnnotatedParameterizedType) reflectionType, overrides);
        } else if (reflectionType instanceof java.lang.reflect.AnnotatedTypeVariable) {
            return new TypeVariableImpl((java.lang.reflect.AnnotatedTypeVariable) reflectionType, overrides);
        } else if (reflectionType instanceof java.lang.reflect.AnnotatedArrayType) {
            return new ArrayTypeImpl((java.lang.reflect.AnnotatedArrayType) reflectionType, overrides);
        } else if (reflectionType instanceof java.lang.reflect.AnnotatedWildcardType) {
            return new WildcardTypeImpl((java.lang.reflect.AnnotatedWildcardType) reflectionType, overrides);
        } else {
            // plain java.lang.reflect.AnnotatedType
            if (reflectionType.getType() instanceof Class) {
                Class<?> clazz = (Class<?>) reflectionType.getType();
                if (clazz.isPrimitive()) {
                    if (clazz == void.class) {
                        return new VoidTypeImpl();
                    } else {
                        return new PrimitiveTypeImpl(reflectionType, overrides);
                    }
                }
                if (clazz.isArray()) {
                    return new ArrayTypeImpl((java.lang.reflect.AnnotatedArrayType) AnnotatedTypes.from(clazz), overrides);
                }
                return new ClassTypeImpl(reflectionType, overrides);
            } else {
                throw LiteExtensionTranslatorLogger.LOG.unknownReflectionType(reflectionType);
            }
        }
    }

    @Override
    public String toString() {
        return reflection.getType().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TypeImpl)) {
            return false;
        }
        TypeImpl<?> type = (TypeImpl<?>) o;
        return Objects.equals(reflection.getType(), type.reflection.getType())
                && Objects.deepEquals(reflection.getAnnotations(), type.reflection.getAnnotations());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(reflection.getType());
        result = 31 * result + Arrays.hashCode(reflection.getAnnotations());
        return result;
    }
}
