package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.AnnotationMember;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.types.Type;
import org.jboss.weld.lite.extension.translator.util.reflection.AnnotatedTypes;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class AnnotationMemberImpl implements AnnotationMember {
    final Kind kind;
    final Object value;

    AnnotationMemberImpl(Object value) {
        this.kind = determineKind(value);
        this.value = value;
    }

    private static Kind determineKind(Object value) {
        if (value instanceof Boolean) {
            return Kind.BOOLEAN;
        } else if (value instanceof Byte) {
            return Kind.BYTE;
        } else if (value instanceof Short) {
            return Kind.SHORT;
        } else if (value instanceof Integer) {
            return Kind.INT;
        } else if (value instanceof Long) {
            return Kind.LONG;
        } else if (value instanceof Float) {
            return Kind.FLOAT;
        } else if (value instanceof Double) {
            return Kind.DOUBLE;
        } else if (value instanceof Character) {
            return Kind.CHAR;
        } else if (value instanceof String) {
            return Kind.STRING;
        } else if (value instanceof Enum) {
            return Kind.ENUM;
        } else if (value instanceof Class) {
            return Kind.CLASS;
        } else if (value instanceof Annotation) {
            return Kind.NESTED_ANNOTATION;
        } else if (value instanceof boolean[]) {
            return Kind.ARRAY;
        } else if (value instanceof byte[]) {
            return Kind.ARRAY;
        } else if (value instanceof short[]) {
            return Kind.ARRAY;
        } else if (value instanceof int[]) {
            return Kind.ARRAY;
        } else if (value instanceof long[]) {
            return Kind.ARRAY;
        } else if (value instanceof float[]) {
            return Kind.ARRAY;
        } else if (value instanceof double[]) {
            return Kind.ARRAY;
        } else if (value instanceof char[]) {
            return Kind.ARRAY;
        } else if (value instanceof Object[]) {
            return Kind.ARRAY;
        } else {
            throw new IllegalArgumentException("Unknown annotation member: " + value);
        }
    }

    private void checkKind(Kind kind) {
        if (this.kind != kind) {
            throw new IllegalStateException("Not " + kind + ": " + value);
        }
    }

    @Override
    public Kind kind() {
        return kind;
    }

    @Override
    public boolean asBoolean() {
        checkKind(Kind.BOOLEAN);
        return (Boolean) value;
    }

    @Override
    public byte asByte() {
        checkKind(Kind.BYTE);
        return (Byte) value;
    }

    @Override
    public short asShort() {
        checkKind(Kind.SHORT);
        return (Short) value;
    }

    @Override
    public int asInt() {
        checkKind(Kind.INT);
        return (Integer) value;
    }

    @Override
    public long asLong() {
        checkKind(Kind.LONG);
        return (Long) value;
    }

    @Override
    public float asFloat() {
        checkKind(Kind.FLOAT);
        return (Float) value;
    }

    @Override
    public double asDouble() {
        checkKind(Kind.DOUBLE);
        return (Double) value;
    }

    @Override
    public char asChar() {
        checkKind(Kind.CHAR);
        return (Character) value;
    }

    @Override
    public String asString() {
        checkKind(Kind.STRING);
        return (String) value;
    }

    @Override
    public <E extends Enum<E>> E asEnum(Class<E> enumType) {
        checkKind(Kind.ENUM);
        return enumType.cast(value);
    }

    @Override
    public String asEnumConstant() {
        checkKind(Kind.ENUM);
        return ((Enum<?>) value).name();
    }

    @Override
    public ClassInfo asEnumClass() {
        checkKind(Kind.ENUM);
        Class<?> enumType = ((Enum<?>) value).getDeclaringClass();
        return new ClassInfoImpl(BeanManagerAccess.createAnnotatedType(enumType));
    }

    @Override
    public Type asType() {
        checkKind(Kind.CLASS);
        Class<?> clazz = (Class<?>) value;
        return TypeImpl.fromReflectionType(AnnotatedTypes.from(clazz));
    }

    @Override
    public AnnotationInfo asNestedAnnotation() {
        checkKind(Kind.NESTED_ANNOTATION);
        return new AnnotationInfoImpl((Annotation) value);
    }

    @Override
    public List<AnnotationMember> asArray() {
        checkKind(Kind.ARRAY);
        List<AnnotationMember> result = new ArrayList<>();
        if (value instanceof boolean[]) {
            for (boolean element : ((boolean[]) value)) {
                result.add(new AnnotationMemberImpl(element));
            }
        } else if (value instanceof byte[]) {
            for (byte element : ((byte[]) value)) {
                result.add(new AnnotationMemberImpl(element));
            }
        } else if (value instanceof short[]) {
            for (short element : ((short[]) value)) {
                result.add(new AnnotationMemberImpl(element));
            }
        } else if (value instanceof int[]) {
            for (int element : ((int[]) value)) {
                result.add(new AnnotationMemberImpl(element));
            }
        } else if (value instanceof long[]) {
            for (long element : ((long[]) value)) {
                result.add(new AnnotationMemberImpl(element));
            }
        } else if (value instanceof float[]) {
            for (float element : ((float[]) value)) {
                result.add(new AnnotationMemberImpl(element));
            }
        } else if (value instanceof double[]) {
            for (double element : ((double[]) value)) {
                result.add(new AnnotationMemberImpl(element));
            }
        } else if (value instanceof char[]) {
            for (char element : ((char[]) value)) {
                result.add(new AnnotationMemberImpl(element));
            }
        } else if (value instanceof Object[]) {
            for (Object element : ((Object[]) value)) {
                result.add(new AnnotationMemberImpl(element));
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AnnotationMemberImpl that = (AnnotationMemberImpl) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "" + value;
    }
}
