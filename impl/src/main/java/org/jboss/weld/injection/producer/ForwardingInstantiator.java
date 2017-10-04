/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.injection.producer;

import java.lang.reflect.Constructor;
import java.util.concurrent.CompletionStage;

import javax.enterprise.context.spi.CreationalContext;

import org.jboss.weld.manager.BeanManagerImpl;

public class ForwardingInstantiator<T> implements Instantiator<T> {

    private final Instantiator<T> delegate;

    public ForwardingInstantiator(Instantiator<T> delegate) {
        this.delegate = delegate;
    }

    protected Instantiator<T> delegate() {
        return delegate;
    }

    @Override
    public T newInstance(CreationalContext<T> ctx, BeanManagerImpl manager) {
        return delegate().newInstance(ctx, manager);
    }

    @Override
    public CompletionStage<T> newInstanceAsync(CreationalContext<T> ctx, BeanManagerImpl manager) {
        return delegate().newInstanceAsync(ctx, manager);
    }

    @Override
    public boolean hasInterceptorSupport() {
        return delegate().hasInterceptorSupport();
    }

    @Override
    public boolean hasDecoratorSupport() {
        return delegate().hasDecoratorSupport();
    }

    @Override
    public Constructor<T> getConstructor() {
        return delegate().getConstructor();
    }
}
