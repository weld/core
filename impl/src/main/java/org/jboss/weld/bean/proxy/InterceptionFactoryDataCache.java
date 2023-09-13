/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bean.proxy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.InterceptionFactory;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.unbacked.UnbackedAnnotatedType;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.injection.producer.InterceptionModelInitializer;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.cache.ComputingCache;
import org.jboss.weld.util.cache.ComputingCacheBuilder;

/**
 * Allows to share data required for effective {@link InterceptionFactory} implementation.
 * <p>
 * This is a per-BeanManager service.
 *
 * @author Martin Kouba
 */
public class InterceptionFactoryDataCache implements Service {

    private static final AtomicLong INDEX = new AtomicLong();

    private final ComputingCache<Key, Optional<InterceptionFactoryData<?>>> cache;

    /**
     *
     * @param beanManager
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public InterceptionFactoryDataCache(BeanManagerImpl beanManager) {
        this.cache = ComputingCacheBuilder.newBuilder().<Key, Optional<InterceptionFactoryData<?>>> build((key) -> {

            ClassTransformer classTransformer = beanManager.getServices().get(ClassTransformer.class);

            long idx = INDEX.incrementAndGet();
            String id = key.annotatedType.getJavaClass().getName() + "$$" + idx;
            UnbackedAnnotatedType<?> slimAnnotatedType = classTransformer.getUnbackedAnnotatedType(key.annotatedType,
                    beanManager.getId(), id);

            EnhancedAnnotatedType<?> enhancedAnnotatedType = classTransformer.getEnhancedAnnotatedType(slimAnnotatedType);

            // Init interception model
            new InterceptionModelInitializer(beanManager, enhancedAnnotatedType,
                    Beans.getBeanConstructor(enhancedAnnotatedType), null).init();
            InterceptionModel interceptionModel = beanManager.getInterceptorModelRegistry().get(slimAnnotatedType);

            boolean hasNonConstructorInterceptors = interceptionModel != null
                    && (interceptionModel.hasExternalNonConstructorInterceptors()
                            || interceptionModel.hasTargetClassInterceptors());

            if (!hasNonConstructorInterceptors) {
                // There are no interceptors to apply
                return Optional.empty();
            }

            Set<MethodSignature> enhancedMethodSignatures = new HashSet<MethodSignature>();
            Set<MethodSignature> interceptedMethodSignatures = new HashSet<MethodSignature>();

            for (AnnotatedMethod<?> method : Beans.getInterceptableMethods(enhancedAnnotatedType)) {
                enhancedMethodSignatures.add(MethodSignatureImpl.of(method));
                if (!interceptionModel.getInterceptors(InterceptionType.AROUND_INVOKE, method.getJavaMember()).isEmpty()) {
                    interceptedMethodSignatures.add(MethodSignatureImpl.of(method));
                }
            }
            InterceptedProxyFactory<?> proxyFactory = new InterceptedProxyFactory<>(beanManager.getContextId(),
                    enhancedAnnotatedType.getJavaClass(),
                    Collections.singleton(enhancedAnnotatedType.getJavaClass()), enhancedMethodSignatures,
                    interceptedMethodSignatures, "" + idx);

            InterceptionFactoryData data = new InterceptionFactoryData(proxyFactory, slimAnnotatedType, interceptionModel);
            return Optional.of(data);
        });
    }

    public <T> Optional<InterceptionFactoryData<T>> getInterceptionFactoryData(AnnotatedType<T> annotatedType) {
        Key key = new Key(AnnotatedTypes.createTypeId(annotatedType), annotatedType);
        try {
            return cache.getCastValue(key);
        } finally {
            key.cleanupAfterUse();
        }
    }

    @Override
    public void cleanup() {
        cache.clear();
    }

    public static class InterceptionFactoryData<T> {

        private final InterceptedProxyFactory<T> interceptedProxyFactory;

        private final SlimAnnotatedType<T> slimAnnotatedType;

        private final InterceptionModel interceptionModel;

        InterceptionFactoryData(InterceptedProxyFactory<T> interceptedProxyFactory, SlimAnnotatedType<T> slimAnnotatedType,
                InterceptionModel interceptionModel) {
            super();
            this.interceptedProxyFactory = interceptedProxyFactory;
            this.slimAnnotatedType = slimAnnotatedType;
            this.interceptionModel = interceptionModel;
        }

        public InterceptedProxyFactory<T> getInterceptedProxyFactory() {
            return interceptedProxyFactory;
        }

        public SlimAnnotatedType<T> getSlimAnnotatedType() {
            return slimAnnotatedType;
        }

        public InterceptionModel getInterceptionModel() {
            return interceptionModel;
        }

    }

    private static class Key {

        private final String typeId;

        private AnnotatedType<?> annotatedType;

        Key(String typeId, AnnotatedType<?> annotatedType) {
            this.typeId = typeId;
            this.annotatedType = annotatedType;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((typeId == null) ? 0 : typeId.hashCode());
            return result;
        }

        void cleanupAfterUse() {
            annotatedType = null;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Key other = (Key) obj;
            if (typeId == null) {
                if (other.typeId != null) {
                    return false;
                }
            } else if (!typeId.equals(other.typeId)) {
                return false;
            }
            return true;
        }

    }

}
