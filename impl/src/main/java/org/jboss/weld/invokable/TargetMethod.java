package org.jboss.weld.invokable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

import jakarta.enterprise.inject.spi.AnnotatedMethod;

public final class TargetMethod {
    private final Method reflection;
    private final AnnotatedMethod<?> cdi;

    public TargetMethod(Method reflectionMethod) {
        this.reflection = reflectionMethod;
        this.cdi = null;
    }

    public TargetMethod(AnnotatedMethod<?> cdiMethod) {
        this.reflection = cdiMethod.getJavaMember();
        this.cdi = cdiMethod;
    }

    public Method getReflection() {
        return reflection;
    }

    public boolean isStatic() {
        return Modifier.isStatic(reflection.getModifiers());
    }

    public int getParameterCount() {
        return reflection.getParameterCount();
    }

    public Type getParameterType(int position) {
        return reflection.getGenericParameterTypes()[position];
    }

    public Collection<Annotation> getParameterAnnotations(int position) {
        if (cdi != null) {
            return cdi.getParameters().get(position).getAnnotations();
        }
        return Arrays.asList(reflection.getParameterAnnotations()[position]);
    }
}
