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

import org.jboss.weld.interceptor.spi.metadata.MethodMetadata;


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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MethodReference that = (MethodReference) o;

        if (declaringClass != null ? !declaringClass.equals(that.declaringClass) : that.declaringClass != null) {
            return false;
        }
        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) {
            return false;
        }
        if (!Arrays.equals(parameterTypes, that.parameterTypes)) {
            return false;
        }

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
}
