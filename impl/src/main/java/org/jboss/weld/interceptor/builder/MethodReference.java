/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.weld.interceptor.builder;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.jboss.weld.interceptor.proxy.InterceptorException;
import org.jboss.weld.interceptor.spi.metadata.MethodMetadata;
import org.jboss.weld.interceptor.util.ReflectionUtils;


/**
 * Wrapper for a method. Allows serializing references to methods.
 *
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public class MethodReference implements Serializable {

    private static final long serialVersionUID = 4080414886347232201L;

    private final String methodName;

    private final Class<?>[] parameterTypes;

    private final Class<?> declaringClass;


    public static MethodReference of(Method method, boolean withDeclaringClass) {
        return new MethodReference(method, withDeclaringClass);
    }

    public static MethodReference of(MethodMetadata method, boolean withDeclaringClass) {
        return new MethodReference(method.getJavaMethod(), withDeclaringClass);
    }

    private MethodReference(Method method, boolean withDeclaringClass) {
        this.methodName = method.getName();
        this.parameterTypes = method.getParameterTypes();
        if (withDeclaringClass) {
            this.declaringClass = method.getDeclaringClass();
        } else {
            this.declaringClass = null;
        }
    }

    private MethodReference(String methodName, Class<?>[] parameterTypes, Class<?> declaringClass) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.declaringClass = declaringClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodReference that = (MethodReference) o;

        if (declaringClass != null ? !declaringClass.equals(that.declaringClass) : that.declaringClass != null)
            return false;
        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;
        if (!Arrays.equals(parameterTypes, that.parameterTypes)) return false;

        return true;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    @Override
    public int hashCode() {
        int result = methodName != null ? methodName.hashCode() : 0;
        result = 31 * result + (parameterTypes != null ? Arrays.hashCode(parameterTypes) : 0);
        result = 31 * result + (declaringClass != null ? declaringClass.hashCode() : 0);
        return result;
    }

    private Object writeReplace() {
        return new MethodHolderSerializationProxy(this);
    }

    static class MethodHolderSerializationProxy implements Serializable {

        private static final long serialVersionUID = -2675263185246118353L;

        private final String className;
        private final String methodName;
        private String[] parameterClassNames;

        MethodHolderSerializationProxy(MethodReference methodReference) {
            className = methodReference.declaringClass != null ? methodReference.declaringClass.getName() : null;
            methodName = methodReference.methodName;
            if (methodReference.parameterTypes != null) {
                parameterClassNames = new String[methodReference.parameterTypes.length];
                int i = 0;
                for (Class<?> parameterType : methodReference.parameterTypes) {
                    parameterClassNames[i++] = parameterType.getName();
                }
            }
        }

        private Object readResolve() {

            try {
                Class<?>[] parameterTypes = null;
                if (parameterClassNames != null) {
                    parameterTypes = new Class<?>[parameterClassNames.length];
                    for (int i = 0; i < parameterClassNames.length; i++) {
                        parameterTypes[i] = ReflectionUtils.classForName(parameterClassNames[i]);
                    }
                }
                Class<?> declaringClass = null;
                if (className != null) {
                    declaringClass = ReflectionUtils.classForName(className);
                }
                return new MethodReference(methodName, parameterTypes, declaringClass);
            } catch (ClassNotFoundException e) {
                throw new InterceptorException("Error while deserializing intercepted instance", e);
            }
        }
    }
}
