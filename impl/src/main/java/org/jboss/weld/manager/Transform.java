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
package org.jboss.weld.manager;

import java.util.Collections;

import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;

import com.google.common.base.Function;

abstract class Transform<T> implements Function<BeanManagerImpl, Iterable<T>> {

    public abstract Iterable<T> transform(BeanManagerImpl input);

    @Override
    public Iterable<T> apply(BeanManagerImpl input) {
        if (input == null) {
            // should never be null but makes findbugs happy
            return Collections.emptySet();
        }
        return transform(input);
    }

    static final Transform<Decorator<?>> DECORATOR = new Transform<Decorator<?>>() {
        @Override
        public Iterable<Decorator<?>> transform(BeanManagerImpl beanManager) {
            return beanManager.getDecorators();
        }
    };

    static final Transform<Interceptor<?>> INTERCEPTOR = new Transform<Interceptor<?>>() {
        @Override
        public Iterable<Interceptor<?>> transform(BeanManagerImpl beanManager) {
            return beanManager.getInterceptors();
        }
    };

    static final Transform<String> NAMESPACE = new Transform<String>() {
        @Override
        public Iterable<String> transform(BeanManagerImpl beanManager) {
            return beanManager.getNamespaces();
        }
    };

    static final Transform<ObserverMethod<?>> OBSERVER = new Transform<ObserverMethod<?>>() {
        @Override
        public Iterable<ObserverMethod<?>> transform(BeanManagerImpl beanManager) {
            return beanManager.getObservers();
        }
    };
}
