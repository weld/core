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

import java.util.concurrent.CompletionStage;

import javax.enterprise.context.spi.CreationalContext;

import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;

public abstract class AbstractInstantiator<T> implements Instantiator<T> {

    @Override
    public T newInstance(CreationalContext<T> ctx, BeanManagerImpl manager) {
        return getConstructorInjectionPoint().newInstance(manager, ctx);
    }

    @Override
    public CompletionStage<T> newInstanceAsync(CreationalContext<T> ctx, BeanManagerImpl manager) {
        return getConstructorInjectionPoint().newInstanceAsync(manager, ctx);
    }

    public abstract ConstructorInjectionPoint<T> getConstructorInjectionPoint();
}
