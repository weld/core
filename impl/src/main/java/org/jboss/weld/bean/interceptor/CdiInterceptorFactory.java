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
package org.jboss.weld.bean.interceptor;

import static org.jboss.weld.util.reflection.Reflections.cast;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Interceptor;

import org.jboss.weld.interceptor.spi.metadata.InterceptorFactory;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * InterceptorFactory that obtains an interceptor instance using {@link BeanManagerImpl}.
 * <p>
 * This factory is used for all {@link Interceptor} implementations.
 *
 * @author Jozef Hartinger
 *
 * @param <T> the type of the interceptor
 */
public class CdiInterceptorFactory<T> implements InterceptorFactory<T> {

    private final Interceptor<T> interceptor;

    public CdiInterceptorFactory(Interceptor<T> interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public T create(CreationalContext<T> ctx, BeanManagerImpl manager) {
        return cast(manager.getReference(interceptor, interceptor.getBeanClass(), ctx, true));
    }

    public Interceptor<T> getInterceptor() {
        return interceptor;
    }

    @Override
    public int hashCode() {
        return interceptor.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof CdiInterceptorFactory) {
            CdiInterceptorFactory<?> that = (CdiInterceptorFactory<?>) obj;
            return this.interceptor.equals(that.interceptor);
        }
        return false;
    }

    @Override
    public String toString() {
        return "CdiInterceptorFactory [interceptor=" + interceptor + "]";
    }
}
