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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;

import org.jboss.weld.interceptor.builder.MethodReference;
import org.jboss.weld.interceptor.spi.metadata.MethodMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.jboss.weld.interceptor.util.InterceptionTypeRegistry;
import org.jboss.weld.resources.SharedObjectFacade;
import org.jboss.weld.util.collections.ArraySet;

/**
 * Represents information about an interceptor method
 *
 * @author Marius Bogoevici
 */
public class DefaultMethodMetadata<M> implements MethodMetadata, Serializable {

    private static final long serialVersionUID = -4538617003189564552L;

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
        this.supportedInterceptorTypes = SharedObjectFacade.wrap(supportedInterceptorTypes);
    }

    private DefaultMethodMetadata(Set<InterceptionType> interceptionTypes, MethodReference methodReference) {
        this.supportedInterceptorTypes = interceptionTypes;
        try {
            this.javaMethod = methodReference.getDeclaringClass().getDeclaredMethod(methodReference.getMethodName(), methodReference.getParameterTypes());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <M> MethodMetadata of(M methodReference, AnnotatedMethodReader<M> methodReader) {
        return new DefaultMethodMetadata<M>(methodReference, methodReader);
    }

    public static MethodMetadata of(Method method) {
        return new DefaultMethodMetadata(method, new ReflectiveAnnotatedMethodReader());
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

    private Object writeReplace() {
        return new DefaultMethodMetadataSerializationProxy(supportedInterceptorTypes, MethodReference.of(this, true));
    }


    private static class DefaultMethodMetadataSerializationProxy implements Serializable {
        private final Set<InterceptionType> supportedInterceptionTypes;
        private final MethodReference methodReference;

        private DefaultMethodMetadataSerializationProxy(Set<InterceptionType> supportedInterceptionTypes, MethodReference methodReference) {
            this.supportedInterceptionTypes = supportedInterceptionTypes;
            this.methodReference = methodReference;
        }

        private Object readResolve() throws ObjectStreamException {
            return new DefaultMethodMetadata(supportedInterceptionTypes, methodReference);
        }

    }

}
