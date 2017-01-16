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
package org.jboss.weld.contexts;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

public abstract class ForwardingContextual<T> implements Contextual<T> {

    protected abstract Contextual<T> delegate();

    public T create(CreationalContext<T> creationalContext) {
        return delegate().create(creationalContext);
    }

    public void destroy(T instance, CreationalContext<T> creationalContext) {
        delegate().destroy(instance, creationalContext);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ForwardingContextual<?>) {
            ForwardingContextual<?> that = (ForwardingContextual<?>) obj;
            return delegate().equals(that.delegate());
        }
        return this == obj || delegate().equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    @Override
    public String toString() {
        return delegate().toString();
    }

}
