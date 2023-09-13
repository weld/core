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

import java.lang.reflect.Method;

import org.jboss.classfilewriter.AccessFlag;
import org.jboss.classfilewriter.util.DescriptorUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class StaticMethodInformation implements MethodInformation {
    private final String name;
    private final String descriptor;
    private final String[] parameterTypes;
    private final String returnType;
    private final String declaringClass;
    private final int modifiers;

    public StaticMethodInformation(String name, Class<?>[] parameterTypes, Class<?> returnType, String declaringClass) {
        this(name, parameterTypes, returnType, declaringClass, AccessFlag.PUBLIC);
    }

    public StaticMethodInformation(String name, Class<?>[] parameterTypes, Class<?> returnType, String declaringClass,
            int modifiers) {
        this.name = name;
        this.parameterTypes = DescriptorUtils.parameterDescriptors(parameterTypes);
        this.returnType = DescriptorUtils.makeDescriptor(returnType);
        this.declaringClass = declaringClass;
        StringBuilder builder = new StringBuilder("(");
        for (String p : this.parameterTypes) {
            builder.append(p);
        }
        builder.append(')');
        builder.append(this.returnType);
        descriptor = builder.toString();
        this.modifiers = modifiers;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public StaticMethodInformation(String name, String[] parameterTypes, String returnType, String declaringClass) {
        this.name = name;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.declaringClass = declaringClass;
        StringBuilder builder = new StringBuilder("(");
        for (String p : this.parameterTypes) {
            builder.append(p);
        }
        builder.append(')');
        builder.append(returnType);
        descriptor = builder.toString();
        this.modifiers = AccessFlag.PUBLIC;
    }

    public String getDeclaringClass() {
        return declaringClass;
    }

    public Method getMethod() {
        return null;
    }

    public String getDescriptor() {
        return descriptor;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getName() {
        return name;
    }

    public int getModifiers() {
        return modifiers;
    }
}
