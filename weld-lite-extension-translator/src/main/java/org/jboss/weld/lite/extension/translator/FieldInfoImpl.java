package org.jboss.weld.lite.extension.translator;

import java.util.Objects;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.declarations.FieldInfo;
import jakarta.enterprise.lang.model.types.Type;

class FieldInfoImpl extends DeclarationInfoImpl<java.lang.reflect.Field, jakarta.enterprise.inject.spi.AnnotatedField<?>>
        implements FieldInfo {
    // only for equals/hashCode
    private final String className;
    private final String name;

    FieldInfoImpl(jakarta.enterprise.inject.spi.AnnotatedField<?> cdiDeclaration, BeanManager bm) {
        super(cdiDeclaration.getJavaMember(), cdiDeclaration, bm);
        this.className = reflection.getDeclaringClass().getName();
        this.name = reflection.getName();
    }

    FieldInfoImpl(java.lang.reflect.Field reflectionDeclaration, BeanManager bm) {
        super(reflectionDeclaration, null, bm);
        this.className = reflectionDeclaration.getDeclaringClass().getName();
        this.name = reflectionDeclaration.getName();
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
        return new ClassInfoImpl(bm.createAnnotatedType(reflection.getDeclaringClass()), bm);
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
