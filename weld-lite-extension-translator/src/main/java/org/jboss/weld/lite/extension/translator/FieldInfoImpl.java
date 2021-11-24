package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.declarations.FieldInfo;
import jakarta.enterprise.lang.model.types.Type;

import java.util.Objects;

class FieldInfoImpl extends DeclarationInfoImpl<java.lang.reflect.Field, jakarta.enterprise.inject.spi.AnnotatedField<?>> implements FieldInfo {
    // only for equals/hashCode
    private final String className;
    private final String name;

    FieldInfoImpl(jakarta.enterprise.inject.spi.AnnotatedField<?> cdiDeclaration) {
        super(cdiDeclaration.getJavaMember(), cdiDeclaration);
        this.className = reflection.getDeclaringClass().getName();
        this.name = reflection.getName();
    }

    FieldInfoImpl(java.lang.reflect.Field reflectionDeclaration) {
        super(reflectionDeclaration, null);
        this.className = reflectionDeclaration.getDeclaringClass().getName();
        this.name = reflectionDeclaration.getName();
    }

    @Override
    public String name() {
        return reflection.getName();
    }

    @Override
    public Type type() {
        return TypeImpl.fromReflectionType(reflection.getAnnotatedType());
    }

    @Override
    public boolean isStatic() {
        return java.lang.reflect.Modifier.isStatic(reflection.getModifiers());
    }

    @Override
    public boolean isFinal() {
        return java.lang.reflect.Modifier.isFinal(reflection.getModifiers());
    }

    @Override
    public int modifiers() {
        return reflection.getModifiers();
    }

    @Override
    public ClassInfo declaringClass() {
        return new ClassInfoImpl(BeanManagerAccess.createAnnotatedType(reflection.getDeclaringClass()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FieldInfoImpl fieldInfo = (FieldInfoImpl) o;
        return Objects.equals(className, fieldInfo.className)
                && Objects.equals(name, fieldInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, name);
    }
}
