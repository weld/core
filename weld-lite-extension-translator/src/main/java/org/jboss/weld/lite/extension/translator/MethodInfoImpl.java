package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.declarations.MethodInfo;
import jakarta.enterprise.lang.model.declarations.ParameterInfo;
import jakarta.enterprise.lang.model.types.Type;
import jakarta.enterprise.lang.model.types.TypeVariable;
import org.jboss.weld.lite.extension.translator.util.reflection.AnnotatedTypes;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

class MethodInfoImpl extends DeclarationInfoImpl<java.lang.reflect.Executable, jakarta.enterprise.inject.spi.AnnotatedCallable<?>> implements MethodInfo {
    // only for equals/hashCode
    private final String className;
    private final String name;
    private final java.lang.reflect.Type[] parameterTypes;

    MethodInfoImpl(jakarta.enterprise.inject.spi.AnnotatedCallable<?> cdiDeclaration) {
        super((java.lang.reflect.Executable) cdiDeclaration.getJavaMember(), cdiDeclaration);
        this.className = reflection.getDeclaringClass().getName();
        this.name = reflection.getName();
        this.parameterTypes = reflection.getGenericParameterTypes();
    }

    MethodInfoImpl(java.lang.reflect.Executable reflectionDeclaration) {
        super(reflectionDeclaration, null);
        this.className = reflectionDeclaration.getDeclaringClass().getName();
        this.name = reflectionDeclaration.getName();
        this.parameterTypes = reflectionDeclaration.getGenericParameterTypes();
    }

    @Override
    public String name() {
        if (isConstructor()) {
            return reflection.getDeclaringClass().getName();
        }
        return reflection.getName();
    }

    @Override
    public List<ParameterInfo> parameters() {
        // CDI doesn't define precisly what `AnnotatedCallable.getParameters` should return,
        // but the language model does -- so here, we return exactly what the lang model
        // defines, but backed by the CDI model as much as possible

        Map<Parameter, jakarta.enterprise.inject.spi.AnnotatedParameter<?>> map = new HashMap<>();
        for (jakarta.enterprise.inject.spi.AnnotatedParameter<?> parameter : cdiDeclaration.getParameters()) {
            map.put(parameter.getJavaParameter(), parameter);
        }

        List<ParameterInfo> result = new ArrayList<>();
        Parameter[] parameters = reflection.getParameters();
        parameters = enumConstructorHack(parameters);

        int position = 0;
        for (Parameter parameter : parameters) {
            if (parameter.isSynthetic()) {
                continue;
            }

            if (map.containsKey(parameter)) {
                result.add(new ParameterInfoImpl(map.get(parameter)));
            } else {
                result.add(new ParameterInfoImpl(parameter, this, position));
            }

            position++;
        }

        return result;
    }

    private Parameter[] enumConstructorHack(Parameter[] parameters) {
        // enum constructors often have 2 synthetic parameters whose `isSynthetic()` returns `false`
        if (isConstructor()
                && reflection.getDeclaringClass().isEnum()
                && reflection.getGenericParameterTypes().length != parameters.length
                && parameters.length >= 2
                && parameters[0].getType().equals(String.class)
                && parameters[1].getType().equals(int.class)) {
            Parameter[] declaredParameters = new Parameter[parameters.length - 2];
            System.arraycopy(parameters, 2, declaredParameters, 0, declaredParameters.length);
            return declaredParameters;
        }
        return parameters;
    }

    @Override
    public Type returnType() {
        return TypeImpl.fromReflectionType(reflection.getAnnotatedReturnType());
    }

    @Override
    public Type receiverType() {
        java.lang.reflect.AnnotatedType receiverType = reflection.getAnnotatedReceiverType();
        if (receiverType == null) {
            return null;
        }

        return TypeImpl.fromReflectionType(receiverType);
    }

    @Override
    public List<Type> throwsTypes() {
        return Arrays.stream(reflection.getAnnotatedExceptionTypes())
                .map(TypeImpl::fromReflectionType)
                .collect(Collectors.toList());
    }

    @Override
    public List<TypeVariable> typeParameters() {
        return Arrays.stream(reflection.getTypeParameters())
                .map(AnnotatedTypes::typeVariable)
                .map(TypeVariableImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isConstructor() {
        return reflection instanceof java.lang.reflect.Constructor;
    }

    @Override
    public boolean isStatic() {
        return java.lang.reflect.Modifier.isStatic(reflection.getModifiers());
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
    public ClassInfo declaringClass() {
        return new ClassInfoImpl(BeanManagerAccess.createAnnotatedType(reflection.getDeclaringClass()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MethodInfoImpl)) {
            return false;
        }
        MethodInfoImpl that = (MethodInfoImpl) o;
        return Objects.equals(className, that.className)
                && Objects.equals(name, that.name)
                && Arrays.equals(parameterTypes, that.parameterTypes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(className, name);
        result = 31 * result + Arrays.hashCode(parameterTypes);
        return result;
    }
}
