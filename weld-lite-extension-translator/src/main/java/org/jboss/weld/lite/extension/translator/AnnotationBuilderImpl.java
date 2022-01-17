package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilder;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.AnnotationMember;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.types.ArrayType;
import jakarta.enterprise.lang.model.types.Type;
import org.jboss.weld.lite.extension.translator.logging.LiteExtensionTranslatorLogger;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

class AnnotationBuilderImpl implements AnnotationBuilder {
    private final Class<? extends Annotation> clazz;
    private final Map<String, AnnotationMember> members = new HashMap<>();
    private final BeanManager bm;

    AnnotationBuilderImpl(Class<? extends Annotation> clazz, BeanManager bm) {
        this.clazz = clazz;
        this.bm = bm;
    }

    @Override
    public AnnotationBuilder member(String name, AnnotationMember value) {
        members.put(name, value);
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, boolean value) {
        members.put(name, new AnnotationMemberImpl(value, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, boolean[] values) {
        members.put(name, new AnnotationMemberImpl(values, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, byte value) {
        members.put(name, new AnnotationMemberImpl(value, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, byte[] values) {
        members.put(name, new AnnotationMemberImpl(values, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, short value) {
        members.put(name, new AnnotationMemberImpl(value, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, short[] values) {
        members.put(name, new AnnotationMemberImpl(values, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, int value) {
        members.put(name, new AnnotationMemberImpl(value, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, int[] values) {
        members.put(name, new AnnotationMemberImpl(values, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, long value) {
        members.put(name, new AnnotationMemberImpl(value, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, long[] values) {
        members.put(name, new AnnotationMemberImpl(values, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, float value) {
        members.put(name, new AnnotationMemberImpl(value, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, float[] values) {
        members.put(name, new AnnotationMemberImpl(values, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, double value) {
        members.put(name, new AnnotationMemberImpl(value, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, double[] values) {
        members.put(name, new AnnotationMemberImpl(values, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, char value) {
        members.put(name, new AnnotationMemberImpl(value, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, char[] values) {
        members.put(name, new AnnotationMemberImpl(values, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, String value) {
        members.put(name, new AnnotationMemberImpl(value, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, String[] values) {
        members.put(name, new AnnotationMemberImpl(values, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, Enum<?> value) {
        members.put(name, new AnnotationMemberImpl(value, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, Enum<?>[] values) {
        members.put(name, new AnnotationMemberImpl(values, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, Class<? extends Enum<?>> enumType, String enumValue) {
        Enum<?> enumConstant = Enum.valueOf((Class) enumType, enumValue);
        members.put(name, new AnnotationMemberImpl(enumConstant, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, Class<? extends Enum<?>> enumType, String[] enumValues) {
        Enum<?>[] enumConstants = new Enum[enumValues.length];
        for (int i = 0; i < enumValues.length; i++) {
            enumConstants[i] = Enum.valueOf((Class) enumType, enumValues[i]);
        }
        members.put(name, new AnnotationMemberImpl(enumConstants, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, ClassInfo enumType, String enumValue) {
        Class enumClass = ((ClassInfoImpl) enumType).cdiDeclaration.getJavaClass();
        Enum<?> enumConstant = Enum.valueOf(enumClass, enumValue);
        members.put(name, new AnnotationMemberImpl(enumConstant, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, ClassInfo enumType, String[] enumValues) {
        Class enumClass = ((ClassInfoImpl) enumType).cdiDeclaration.getJavaClass();
        Enum<?>[] enumConstants = new Enum[enumValues.length];
        for (int i = 0; i < enumValues.length; i++) {
            enumConstants[i] = Enum.valueOf(enumClass, enumValues[i]);
        }
        members.put(name, new AnnotationMemberImpl(enumConstants, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, Class<?> value) {
        members.put(name, new AnnotationMemberImpl(value, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, Class<?>[] values) {
        members.put(name, new AnnotationMemberImpl(values, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, ClassInfo value) {
        Class<?> clazz = ((ClassInfoImpl) value).cdiDeclaration.getJavaClass();
        members.put(name, new AnnotationMemberImpl(clazz, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, ClassInfo[] values) {
        Class<?>[] classes = new Class[values.length];
        for (int i = 0; i < values.length; i++) {
            classes[i] = ((ClassInfoImpl) values[i]).cdiDeclaration.getJavaClass();
        }
        members.put(name, new AnnotationMemberImpl(classes, bm));
        return this;
    }

    private Class<?> validateType(Type type) {
        if (type instanceof VoidTypeImpl) {
            return ((VoidTypeImpl) type).clazz;
        } else if (type instanceof PrimitiveTypeImpl) {
            return ((PrimitiveTypeImpl) type).clazz;
        } else if (type instanceof ClassTypeImpl) {
            return ((ClassTypeImpl) type).clazz;
        } else if (type instanceof ArrayTypeImpl) {
            ArrayType arrayType = type.asArray();
            Type elementType = arrayType.componentType();
            while (elementType.isArray()) {
                elementType = elementType.asArray().componentType();
            }
            if (elementType instanceof PrimitiveTypeImpl) {
                return ((PrimitiveTypeImpl) elementType).clazz;
            } else if (elementType instanceof ClassTypeImpl) {
                return ((ClassTypeImpl) elementType).clazz;
            }
        }

        throw LiteExtensionTranslatorLogger.LOG.illegalAnnotationMemberType(type);
    }

    @Override
    public AnnotationBuilder member(String name, Type value) {
        Class<?> clazz = validateType(value);
        members.put(name, new AnnotationMemberImpl(clazz, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, Type[] values) {
        Class<?>[] classes = new Class[values.length];
        for (int i = 0; i < values.length; i++) {
            classes[i] = validateType(values[i]);
        }
        members.put(name, new AnnotationMemberImpl(classes, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, AnnotationInfo value) {
        Annotation annotation = ((AnnotationInfoImpl) value).annotation;
        members.put(name, new AnnotationMemberImpl(annotation, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, AnnotationInfo[] values) {
        Annotation[] annotations = new Annotation[values.length];
        for (int i = 0; i < values.length; i++) {
            annotations[i] = ((AnnotationInfoImpl) values[i]).annotation;
        }
        members.put(name, new AnnotationMemberImpl(annotations, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, Annotation value) {
        members.put(name, new AnnotationMemberImpl(value, bm));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, Annotation[] values) {
        members.put(name, new AnnotationMemberImpl(values, bm));
        return this;
    }

    @Override
    public AnnotationInfo build() {
        Annotation annotation = AnnotationProxy.create(clazz, members);
        return new AnnotationInfoImpl(annotation, bm);
    }
}
