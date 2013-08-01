/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
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

package org.jboss.weld.interceptor.reader;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

import org.jboss.weld.interceptor.spi.metadata.MethodMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.jboss.weld.interceptor.util.InterceptionTypeRegistry;
import org.jboss.weld.util.collections.ArraySet;

/**
 * Represents information about an interceptor method
 *
 * @author Marius Bogoevici
 * @author Jozef Hartinger
 * @author Marko Luksa
 */
public class DefaultMethodMetadata<M> implements MethodMetadata {

    private final Method javaMethod;

    private final Set<InterceptionType> supportedInterceptorTypes;

    private DefaultMethodMetadata(M methodReference, AnnotatedMethodReader<M> annotationReader) {
        this.javaMethod = annotationReader.getJavaMethod(methodReference);
        ArraySet<InterceptionType> supportedInterceptorTypes = new ArraySet<InterceptionType>();
        for (InterceptionType interceptionType : InterceptionTypeRegistry.getSupportedInterceptionTypes()) {
            if (annotationReader.getAnnotation(InterceptionTypeRegistry.getAnnotationClass(interceptionType), methodReference) != null) {
                supportedInterceptorTypes.add(interceptionType);
            }
        }
        if (supportedInterceptorTypes.isEmpty()) {
            this.supportedInterceptorTypes = Collections.emptySet();
        } else {
            this.supportedInterceptorTypes = supportedInterceptorTypes;
        }
    }

    public static <M> MethodMetadata of(M methodReference, AnnotatedMethodReader<M> methodReader) {
        return new DefaultMethodMetadata<M>(methodReference, methodReader);
    }

    public static MethodMetadata of(Method method) {
        return new DefaultMethodMetadata(method, new ReflectiveAnnotatedMethodReader());
    }

    @Override
    public boolean isInterceptorMethod() {
        return !supportedInterceptorTypes.isEmpty();
    }

    public Set<InterceptionType> getSupportedInterceptionTypes() {
        return supportedInterceptorTypes;
    }

    public Method getJavaMethod() {
        return javaMethod;
    }

    public Class<?> getReturnType() {
        return javaMethod.getReturnType();
    }
}
