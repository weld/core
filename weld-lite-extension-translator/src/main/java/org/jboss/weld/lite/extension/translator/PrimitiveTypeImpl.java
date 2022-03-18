package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.types.PrimitiveType;
import org.jboss.weld.lite.extension.translator.logging.LiteExtensionTranslatorLogger;
import org.jboss.weld.lite.extension.translator.util.AnnotationOverrides;
import org.jboss.weld.lite.extension.translator.util.reflection.AnnotatedTypes;

import java.lang.reflect.AnnotatedType;

class PrimitiveTypeImpl extends TypeImpl<AnnotatedType> implements PrimitiveType {
    final Class<?> clazz;

    PrimitiveTypeImpl(AnnotatedType primitiveType, BeanManager bm) {
        this(primitiveType, null, bm);
    }

    PrimitiveTypeImpl(AnnotatedType primitiveType, AnnotationOverrides overrides, BeanManager bm) {
        super(primitiveType, overrides, bm);
        this.clazz = (Class<?>) primitiveType.getType();
    }

    PrimitiveTypeImpl(Class<?> primitiveType, BeanManager bm) {
        this(primitiveType, null, bm);
    }

    PrimitiveTypeImpl(Class<?> primitiveType, AnnotationOverrides overrides, BeanManager bm) {
        super(AnnotatedTypes.from(primitiveType), overrides, bm);
        this.clazz = primitiveType;
    }

    @Override
    public String name() {
        return reflection.getType().getTypeName();
    }

    @Override
    public PrimitiveKind primitiveKind() {
        if (clazz == boolean.class) {
            return PrimitiveKind.BOOLEAN;
        } else if (clazz == byte.class) {
            return PrimitiveKind.BYTE;
        } else if (clazz == short.class) {
            return PrimitiveKind.SHORT;
        } else if (clazz == int.class) {
            return PrimitiveKind.INT;
        } else if (clazz == long.class) {
            return PrimitiveKind.LONG;
        } else if (clazz == float.class) {
            return PrimitiveKind.FLOAT;
        } else if (clazz == double.class) {
            return PrimitiveKind.DOUBLE;
        } else if (clazz == char.class) {
            return PrimitiveKind.CHAR;
        } else {
            throw LiteExtensionTranslatorLogger.LOG.unknownPrimitiveType(clazz);
        }
    }
}
