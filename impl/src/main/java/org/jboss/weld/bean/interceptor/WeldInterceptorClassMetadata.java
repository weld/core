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

package org.jboss.weld.bean.interceptor;

import static org.jboss.weld.util.collections.WeldCollections.immutableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.interceptor.reader.DefaultMethodMetadata;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.MethodMetadata;

/**
 * @author Marius Bogoevici
 */
public class WeldInterceptorClassMetadata<T> implements ClassMetadata<T>, Serializable {
    private static final long serialVersionUID = -5087425231467781559L;

    private Class<T> clazz;

    private WeldInterceptorClassMetadata<?> superclass;

    private Collection<MethodMetadata> methodMetadatas;

    private WeldInterceptorClassMetadata(EnhancedAnnotatedType<T> weldClass) {
        this.clazz = weldClass.getJavaClass();
        ArrayList<MethodMetadata> methodMetadatas = new ArrayList<MethodMetadata>();
        for (EnhancedAnnotatedMethod<?, ?> method : weldClass.getDeclaredEnhancedMethods()) {
            MethodMetadata methodMetadata = DefaultMethodMetadata.of(method, WeldAnnotatedMethodReader.getInstance());
            if (methodMetadata.getSupportedInterceptionTypes() != null && methodMetadata.getSupportedInterceptionTypes().size() != 0) {
                methodMetadatas.add(methodMetadata);
            }
        }
        this.methodMetadatas = immutableList(methodMetadatas);
        if (weldClass.getEnhancedSuperclass() != null) {
            this.superclass = WeldInterceptorClassMetadata.of(weldClass.getEnhancedSuperclass());
        }
    }

    public static <T> WeldInterceptorClassMetadata<T> of(EnhancedAnnotatedType<T> weldClass) {
        return new WeldInterceptorClassMetadata<T>(weldClass);
    }

    public String getClassName() {
        return clazz.getName();
    }

    public Iterable<MethodMetadata> getDeclaredMethods() {
        return methodMetadatas;
    }

    public Class<T> getJavaClass() {
        return clazz;
    }

    public ClassMetadata<?> getSuperclass() {
        return superclass;
    }

}

