package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.Types;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.types.ArrayType;
import jakarta.enterprise.lang.model.types.ClassType;
import jakarta.enterprise.lang.model.types.ParameterizedType;
import jakarta.enterprise.lang.model.types.PrimitiveType;
import jakarta.enterprise.lang.model.types.Type;
import jakarta.enterprise.lang.model.types.VoidType;
import jakarta.enterprise.lang.model.types.WildcardType;
import org.jboss.weld.lite.extension.translator.logging.LiteExtensionTranslatorLogger;
import org.jboss.weld.lite.extension.translator.util.reflection.AnnotatedTypes;

class TypesImpl implements Types {

    private final BeanManager bm;

    public TypesImpl(BeanManager bm) {
        this.bm = bm;
    }

    @Override
    public Type of(Class<?> clazz) {
        if (clazz.isArray()) {
            int dimensions = 1;
            Class<?> componentType = clazz.getComponentType();
            while (componentType.isArray()) {
                dimensions++;
                componentType = componentType.getComponentType();
            }
            return ofArray(of(componentType), dimensions);
        }

        if (clazz.isPrimitive()) {
            if (clazz == void.class) {
                return ofVoid();
            } else if (clazz == boolean.class) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.BOOLEAN);
            } else if (clazz == byte.class) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.BYTE);
            } else if (clazz == short.class) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.SHORT);
            } else if (clazz == int.class) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.INT);
            } else if (clazz == long.class) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.LONG);
            } else if (clazz == float.class) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.FLOAT);
            } else if (clazz == double.class) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.DOUBLE);
            } else if (clazz == char.class) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.CHAR);
            } else {
                throw LiteExtensionTranslatorLogger.LOG.unknownPrimitiveType(clazz);
            }
        }

        return new ClassTypeImpl(clazz, bm);
    }

    @Override
    public VoidType ofVoid() {
        return new VoidTypeImpl(bm);
    }

    @Override
    public PrimitiveType ofPrimitive(PrimitiveType.PrimitiveKind kind) {
        switch (kind) {
            case BOOLEAN:
                return new PrimitiveTypeImpl(Boolean.TYPE, bm);
            case BYTE:
                return new PrimitiveTypeImpl(Byte.TYPE, bm);
            case SHORT:
                return new PrimitiveTypeImpl(Short.TYPE, bm);
            case INT:
                return new PrimitiveTypeImpl(Integer.TYPE, bm);
            case LONG:
                return new PrimitiveTypeImpl(Long.TYPE, bm);
            case FLOAT:
                return new PrimitiveTypeImpl(Float.TYPE, bm);
            case DOUBLE:
                return new PrimitiveTypeImpl(Double.TYPE, bm);
            case CHAR:
                return new PrimitiveTypeImpl(Character.TYPE, bm);
            default:
                throw LiteExtensionTranslatorLogger.LOG.unknownPrimitiveType(kind);
        }
    }

    @Override
    public ClassType ofClass(String name) {
        try {
            Class<?> clazz = Class.forName(name, true, Thread.currentThread().getContextClassLoader());
            return new ClassTypeImpl(clazz, bm);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Override
    public ClassType ofClass(ClassInfo clazz) {
        return (ClassType) of(((ClassInfoImpl) clazz).cdiDeclaration.getJavaClass());
    }

    @Override
    public ArrayType ofArray(Type elementType, int dimensions) {
        return new ArrayTypeImpl(AnnotatedTypes.array(((TypeImpl<?>) elementType).reflection.getType(), dimensions), bm);
    }

    @Override
    public ParameterizedType parameterized(Class<?> genericType, Class<?>... typeArguments) {
        return new ParameterizedTypeImpl(AnnotatedTypes.parameterized(genericType, typeArguments), bm);
    }

    @Override
    public ParameterizedType parameterized(Class<?> genericType, Type... typeArguments) {
        java.lang.reflect.Type[] underlyingTypeArguments = new java.lang.reflect.Type[typeArguments.length];
        for (int i = 0; i < typeArguments.length; i++) {
            underlyingTypeArguments[i] = ((TypeImpl<?>) typeArguments[i]).reflection.getType();
        }
        return new ParameterizedTypeImpl(AnnotatedTypes.parameterized(genericType, underlyingTypeArguments), bm);
    }

    @Override
    public ParameterizedType parameterized(ClassType genericType, Type... typeArguments) {
        Class<?> clazz = (Class<?>) ((TypeImpl<?>) genericType).reflection.getType();
        java.lang.reflect.Type[] underlyingTypeArguments = new java.lang.reflect.Type[typeArguments.length];
        for (int i = 0; i < typeArguments.length; i++) {
            underlyingTypeArguments[i] = ((TypeImpl<?>) typeArguments[i]).reflection.getType();
        }
        return new ParameterizedTypeImpl(AnnotatedTypes.parameterized(clazz, underlyingTypeArguments), bm);
    }

    @Override
    public WildcardType wildcardWithUpperBound(Type upperBound) {
        return new WildcardTypeImpl(AnnotatedTypes.wildcardWithUpperBound(((TypeImpl<?>) upperBound).reflection.getType()), bm);
    }

    @Override
    public WildcardType wildcardWithLowerBound(Type lowerBound) {
        return new WildcardTypeImpl(AnnotatedTypes.wildcardWithLowerBound(((TypeImpl<?>) lowerBound).reflection.getType()), bm);
    }

    @Override
    public WildcardType wildcardUnbounded() {
        return new WildcardTypeImpl(AnnotatedTypes.unboundedWildcardType(), bm);
    }
}
