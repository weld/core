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

import static org.jboss.weld.logging.messages.UtilMessage.UNABLE_TO_FIND_CONSTRUCTOR;

import java.lang.reflect.Constructor;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorFactory;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.SecureReflections;

public class ClassMetadataInterceptorFactory<T> implements InterceptorFactory<T> {

    public static <T> InterceptorFactory<T> of(ClassMetadata<T> classMetadata, BeanManagerImpl manager) {
        return new ClassMetadataInterceptorFactory<T>(classMetadata, manager);
    }

    private final ClassMetadata<T> classMetadata;
    private final InjectionTarget<T> injectionTarget;
    private final Constructor<T> constructor;

    private ClassMetadataInterceptorFactory(ClassMetadata<T> classMetadata, BeanManagerImpl manager) {
        this.classMetadata = classMetadata;
        this.injectionTarget = manager.createInjectionTarget(manager.createAnnotatedType(classMetadata.getJavaClass()));
        try {
            this.constructor = SecureReflections.ensureAccessible(SecureReflections.getDeclaredConstructor(classMetadata.getJavaClass()));
        } catch (NoSuchMethodException e) {
            throw new DefinitionException(UNABLE_TO_FIND_CONSTRUCTOR, e);
        }
    }

    @Override
    public ClassMetadata<T> getClassMetadata() {
        return classMetadata;
    }

    public T create(CreationalContext<T> ctx, BeanManager manager) {
        try {
            T instance = constructor.newInstance(); // TODO: use special InjectionTarget (that does not apply interceptors) to instantiate the class
            injectionTarget.inject(instance, ctx);
            return instance;
        } catch (Exception e) {
            throw new CreationException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ClassMetadataInterceptorFactory<?>) {
            ClassMetadataInterceptorFactory<?> that = (ClassMetadataInterceptorFactory<?>) o;
            return this.classMetadata.equals(that.classMetadata);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return classMetadata.hashCode();
    }

    @Override
    public String toString() {
        return "ClassMetadataInterceptorFactory [class=" + classMetadata.getJavaClass().getName() + "]";
    }
}
