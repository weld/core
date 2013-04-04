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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.Interceptor;
import javax.interceptor.Interceptors;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.injection.producer.BasicInjectionTarget;
import org.jboss.weld.injection.producer.LifecycleCallbackInvoker;
import org.jboss.weld.injection.producer.NoopLifecycleCallbackInvoker;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorFactory;
import org.jboss.weld.manager.BeanManagerImpl;

public class ClassMetadataInterceptorFactory<T> implements InterceptorFactory<T> {

    public static <T> InterceptorFactory<T> of(ClassMetadata<T> classMetadata, BeanManagerImpl manager) {
        return new ClassMetadataInterceptorFactory<T>(classMetadata, manager);
    }

    private final ClassMetadata<T> classMetadata;
    private final InjectionTarget<T> injectionTarget;

    private ClassMetadataInterceptorFactory(ClassMetadata<T> classMetadata, BeanManagerImpl manager) {
        this.classMetadata = classMetadata;
        this.injectionTarget = new InterceptorInjectionTarget<T>(manager.createEnhancedAnnotatedType(classMetadata.getJavaClass()), manager);
    }

    @Override
    public ClassMetadata<T> getClassMetadata() {
        return classMetadata;
    }

    public T create(CreationalContext<T> ctx, BeanManager manager) {
        T instance = injectionTarget.produce(ctx);
        injectionTarget.inject(instance, ctx);
        return instance;
    }

    public InjectionTarget<T> getInjectionTarget() {
        return injectionTarget;
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

    /**
     * {@link InjectionTarget} for interceptors which do not have associated {@link Interceptor}. These interceptors are a
     * result of using {@link Interceptors} annotation directly on the target class.
     *
     * @author Jozef Hartinger
     *
     * @param <T>
     */
    private static class InterceptorInjectionTarget<T> extends BasicInjectionTarget<T> {

        public InterceptorInjectionTarget(EnhancedAnnotatedType<T> type, BeanManagerImpl beanManager) {
            super(type, null, beanManager);
        }

        @Override
        protected LifecycleCallbackInvoker<T> initInvoker(EnhancedAnnotatedType<T> type) {
            return NoopLifecycleCallbackInvoker.getInstance();
        }
    }
}
