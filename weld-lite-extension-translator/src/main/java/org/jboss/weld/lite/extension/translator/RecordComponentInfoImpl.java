package org.jboss.weld.lite.extension.translator;

import java.util.Objects;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.declarations.FieldInfo;
import jakarta.enterprise.lang.model.declarations.MethodInfo;
import jakarta.enterprise.lang.model.declarations.RecordComponentInfo;
import jakarta.enterprise.lang.model.types.Type;

class RecordComponentInfoImpl
        extends DeclarationInfoImpl<java.lang.reflect.RecordComponent, jakarta.enterprise.inject.spi.AnnotatedMember<?>>
        implements RecordComponentInfo {
    // only for equals/hashCode
    private final String className;
    private final String name;

    RecordComponentInfoImpl(java.lang.reflect.RecordComponent reflectionDeclaration, BeanManager bm) {
        super(reflectionDeclaration, null, bm);
        this.className = reflection.getDeclaringRecord().getName();
        this.name = reflection.getName();
    }

    @Override
    public String name() {
        return reflection.getName();
    }

    @Override
    public Type type() {
        return TypeImpl.fromReflectionType(reflection.getAnnotatedType(), bm);
    }

    @Override
    public FieldInfo field() {
        try {
            java.lang.reflect.Field field = reflection.getDeclaringRecord().getDeclaredField(reflection.getName());
            return new FieldInfoImpl(field, bm);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MethodInfo accessor() {
        return new MethodInfoImpl(reflection.getAccessor(), bm);
    }

    @Override
    public ClassInfo declaringRecord() {
        return new ClassInfoImpl(bm.createAnnotatedType(reflection.getDeclaringRecord()), bm);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecordComponentInfoImpl)) {
            return false;
        }
        RecordComponentInfoImpl that = (RecordComponentInfoImpl) o;
        return Objects.equals(className, that.className)
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, name);
    }
}
