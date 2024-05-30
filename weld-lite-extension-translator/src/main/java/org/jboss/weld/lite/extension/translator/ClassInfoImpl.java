package org.jboss.weld.lite.extension.translator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.declarations.FieldInfo;
import jakarta.enterprise.lang.model.declarations.MethodInfo;
import jakarta.enterprise.lang.model.declarations.PackageInfo;
import jakarta.enterprise.lang.model.declarations.RecordComponentInfo;
import jakarta.enterprise.lang.model.types.Type;
import jakarta.enterprise.lang.model.types.TypeVariable;

import org.jboss.weld.lite.extension.translator.util.reflection.AnnotatedTypes;

class ClassInfoImpl extends DeclarationInfoImpl<Class<?>, jakarta.enterprise.inject.spi.AnnotatedType<?>> implements ClassInfo {
    // only for equals/hashCode
    private final String name;

    ClassInfoImpl(jakarta.enterprise.inject.spi.AnnotatedType<?> cdiDeclaration, BeanManager bm) {
        super(cdiDeclaration.getJavaClass(), cdiDeclaration, bm);
        this.name = cdiDeclaration.getJavaClass().getName();
    }

    @Override
    public String name() {
        return reflection.getName();
    }

    @Override
    public String simpleName() {
        return reflection.getSimpleName();
    }

    @Override
    public PackageInfo packageInfo() {
        return new PackageInfoImpl(reflection.getPackage(), bm);
    }

    @Override
    public List<TypeVariable> typeParameters() {
        return Arrays.stream(reflection.getTypeParameters())
                .map(AnnotatedTypes::typeVariable)
                .map(annotatedTypeVariable -> new TypeVariableImpl(annotatedTypeVariable, bm))
                .collect(Collectors.toList());
    }

    @Override
    public Type superClass() {
        java.lang.reflect.AnnotatedType superClass = reflection.getAnnotatedSuperclass();
        return superClass != null ? TypeImpl.fromReflectionType(superClass, bm) : null;
    }

    @Override
    public ClassInfo superClassDeclaration() {
        Class<?> superClass = reflection.getSuperclass();
        return superClass != null ? new ClassInfoImpl(bm.createAnnotatedType(superClass), bm) : null;
    }

    @Override
    public List<Type> superInterfaces() {
        java.lang.reflect.AnnotatedType[] interfaces = reflection.getAnnotatedInterfaces();
        return Arrays.stream(interfaces)
                .map(annotatedType -> TypeImpl.fromReflectionType(annotatedType, bm))
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassInfo> superInterfacesDeclarations() {
        return Arrays.stream(reflection.getInterfaces())
                .map(it -> new ClassInfoImpl(bm.createAnnotatedType(it), bm))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isPlainClass() {
        return !isInterface() && !isEnum() && !isAnnotation() && !isRecord();
    }

    @Override
    public boolean isInterface() {
        if (isAnnotation()) {
            return false;
        }
        return reflection.isInterface();
    }

    @Override
    public boolean isEnum() {
        return reflection.isEnum();
    }

    @Override
    public boolean isAnnotation() {
        return reflection.isAnnotation();
    }

    @Override
    public boolean isRecord() {
        Class<?> superclass = reflection.getSuperclass();
        return superclass != null && superclass.getName().equals("java.lang.Record");
    }

    @Override
    public boolean isAbstract() {
        return java.lang.reflect.Modifier.isAbstract(reflection.getModifiers());
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
    public Collection<MethodInfo> constructors() {
        // CDI doesn't define precisly what `AnnotatedType.getConstructors` should return,
        // but the language model does -- so here, we return exactly what the lang model
        // defines, but backed by the CDI model as much as possible

        Map<java.lang.reflect.Constructor<?>, jakarta.enterprise.inject.spi.AnnotatedConstructor<?>> map = new HashMap<>();
        for (jakarta.enterprise.inject.spi.AnnotatedConstructor<?> constructor : cdiDeclaration.getConstructors()) {
            map.put(constructor.getJavaMember(), constructor);
        }

        return Arrays.stream(reflection.getDeclaredConstructors())
                .filter(it -> !it.isSynthetic())
                .map(it -> {
                    if (map.containsKey(it)) {
                        return new MethodInfoImpl(map.get(it), bm);
                    } else {
                        return new MethodInfoImpl(it, bm);
                    }
                }).collect(Collectors.toList());
    }

    @Override
    public Collection<MethodInfo> methods() {
        // CDI doesn't define precisly what `AnnotatedType.getMethods` should return,
        // but the language model does -- so here, we return exactly what the lang model
        // defines, but backed by the CDI model as much as possible

        Map<java.lang.reflect.Method, jakarta.enterprise.inject.spi.AnnotatedMethod<?>> map = new HashMap<>();
        for (jakarta.enterprise.inject.spi.AnnotatedMethod<?> method : cdiDeclaration.getMethods()) {
            map.put(method.getJavaMember(), method);
        }

        return ReflectionMembers.allMethods(reflection)
                .stream()
                .filter(it -> !it.isSynthetic())
                .map(it -> {
                    if (map.containsKey(it)) {
                        return new MethodInfoImpl(map.get(it), bm);
                    } else {
                        return new MethodInfoImpl(it, bm);
                    }
                }).collect(Collectors.toList());
    }

    @Override
    public Collection<FieldInfo> fields() {
        // CDI doesn't define precisly what `AnnotatedType.getFields` should return,
        // but the language model does -- so here, we return exactly what the lang model
        // defines, but backed by the CDI model as much as possible

        Map<java.lang.reflect.Field, jakarta.enterprise.inject.spi.AnnotatedField<?>> map = new HashMap<>();
        for (jakarta.enterprise.inject.spi.AnnotatedField<?> field : cdiDeclaration.getFields()) {
            map.put(field.getJavaMember(), field);
        }

        return ReflectionMembers.allFields(reflection)
                .stream()
                .filter(it -> !it.isSynthetic())
                .map(it -> {
                    if (map.containsKey(it)) {
                        return new FieldInfoImpl(map.get(it), bm);
                    } else {
                        return new FieldInfoImpl(it, bm);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public Collection<RecordComponentInfo> recordComponents() {
        if (isRecord()) {
            // Records are not supported yet because base JDK version for lang model is 11
            throw new UnsupportedOperationException("Records not yet supported");
        }
        return Collections.emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClassInfoImpl classInfo = (ClassInfoImpl) o;
        return Objects.equals(name, classInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
