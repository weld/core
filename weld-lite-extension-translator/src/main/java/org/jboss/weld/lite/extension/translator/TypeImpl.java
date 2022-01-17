package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.types.Type;
import org.jboss.weld.lite.extension.translator.logging.LiteExtensionTranslatorLogger;
import org.jboss.weld.lite.extension.translator.util.AnnotationOverrides;
import org.jboss.weld.lite.extension.translator.util.reflection.AnnotatedTypes;

import java.util.Arrays;
import java.util.Objects;

abstract class TypeImpl<ReflectionType extends java.lang.reflect.AnnotatedType> extends AnnotationTargetImpl<ReflectionType> implements Type {
    TypeImpl(ReflectionType reflectionType, AnnotationOverrides overrides, BeanManager bm) {
        super(reflectionType, overrides, bm);
    }

    static Type fromReflectionType(java.lang.reflect.AnnotatedType reflectionType, BeanManager bm) {
        return fromReflectionType(reflectionType, null, bm);
    }

    static Type fromReflectionType(java.lang.reflect.AnnotatedType reflectionType, AnnotationOverrides overrides, BeanManager bm) {
        if (reflectionType instanceof java.lang.reflect.AnnotatedParameterizedType) {
            return new ParameterizedTypeImpl((java.lang.reflect.AnnotatedParameterizedType) reflectionType, overrides, bm);
        } else if (reflectionType instanceof java.lang.reflect.AnnotatedTypeVariable) {
            return new TypeVariableImpl((java.lang.reflect.AnnotatedTypeVariable) reflectionType, overrides, bm);
        } else if (reflectionType instanceof java.lang.reflect.AnnotatedArrayType) {
            return new ArrayTypeImpl((java.lang.reflect.AnnotatedArrayType) reflectionType, overrides, bm);
        } else if (reflectionType instanceof java.lang.reflect.AnnotatedWildcardType) {
            return new WildcardTypeImpl((java.lang.reflect.AnnotatedWildcardType) reflectionType, overrides, bm);
        } else {
            // plain java.lang.reflect.AnnotatedType
            if (reflectionType.getType() instanceof Class) {
                Class<?> clazz = (Class<?>) reflectionType.getType();
                if (clazz.isPrimitive()) {
                    if (clazz == void.class) {
                        return new VoidTypeImpl(bm);
                    } else {
                        return new PrimitiveTypeImpl(reflectionType, overrides, bm);
                    }
                }
                if (clazz.isArray()) {
                    return new ArrayTypeImpl((java.lang.reflect.AnnotatedArrayType) AnnotatedTypes.from(clazz), overrides, bm);
                }
                return new ClassTypeImpl(reflectionType, overrides, bm);
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
