/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.serialization;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.enterprise.inject.spi.AnnotatedMethod;

import org.jboss.weld.interceptor.spi.metadata.MethodMetadata;
import org.jboss.weld.logging.ReflectionLogger;

import com.google.common.base.Objects;

/**
 * Serializable holder for {@link Method}.
 *
 * @author Jozef Hartinger
 *
 */
public class MethodHolder extends AbstractSerializableHolder<Method> implements PrivilegedAction<Method> {

    private static final long serialVersionUID = -3033089710155551280L;

    private final Class<?> declaringClass;
    private final String methodName;
    private final Class<?>[] parameterTypes;

    public MethodHolder(Method method) {
        super(method);
        this.declaringClass = method.getDeclaringClass();
        this.methodName = method.getName();
        this.parameterTypes = method.getParameterTypes();
    }

    public static MethodHolder of(Method method) {
        return new MethodHolder(method);
    }

    public static MethodHolder of(AnnotatedMethod<?> method) {
        return new MethodHolder(method.getJavaMember());
    }

    public static MethodHolder of(MethodMetadata method) {
        return new MethodHolder(method.getJavaMethod());
    }

    @Override
    protected Method initialize() {
        return AccessController.doPrivileged(this);
    }

    @Override
    public Method run() {
        try {
            return declaringClass.getDeclaredMethod(methodName, parameterTypes);
        } catch (Exception e) {
            throw ReflectionLogger.LOG.unableToGetMethodOnDeserialization(declaringClass, parameterTypes, e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MethodHolder that = (MethodHolder) o;
        return Objects.equal(get(), that.get());
    }

    @Override
    public int hashCode() {
        return get().hashCode();
    }

}
