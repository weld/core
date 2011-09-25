/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.util.bytecode;

import javassist.bytecode.AccessFlag;

import java.lang.reflect.Method;

/**
 * Contains all the data that is needed when working with a method in bytecode
 *
 * @author Stuart Douglas
 */
public class RuntimeMethodInformation implements MethodInformation {
    private final Method method;
    private final String descriptor;
    private final String[] parameterTypes;
    private final String returnType;
    private final String declaringClass;
    private final int modifier;

    public RuntimeMethodInformation(Method method) {
        this.method = method;
        this.parameterTypes = DescriptorUtils.getParameterTypes(method);
        this.returnType = DescriptorUtils.classToStringRepresentation(method.getReturnType());
        this.descriptor = DescriptorUtils.getMethodDescriptor(parameterTypes, returnType);
        this.declaringClass = method.getDeclaringClass().getName();
        if (method.isBridge()) {
            modifier = AccessFlag.PUBLIC | AccessFlag.BRIDGE | AccessFlag.SYNTHETIC;
        } else {
            modifier = AccessFlag.PUBLIC;
        }
    }

    public String getDeclaringClass() {
        return declaringClass;
    }

    public Method getMethod() {
        return method;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getName() {
        return method.getName();
    }

    public int getModifiers() {
        return modifier;
    }
}
