/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.util.bean;

import java.lang.annotation.Annotation;
import java.util.Iterator;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

/**
 *
 * @author Martin Kouba
 *
 * @param <T>
 */
public abstract class ForwardingInstance<T> implements Instance<T> {

    protected abstract Instance<T> delegate();

    @Override
    public Iterator<T> iterator() {
        return delegate().iterator();
    }

    @Override
    public T get() {
        return delegate().get();
    }

    @Override
    public Instance<T> select(Annotation... qualifiers) {
        return delegate().select(qualifiers);
    }

    @Override
    public <U extends T> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
        return delegate().select(subtype, qualifiers);
    }

    @Override
    public <U extends T> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
        return delegate().select(subtype, qualifiers);
    }

    @Override
    public boolean isUnsatisfied() {
        return delegate().isUnsatisfied();
    }

    @Override
    public boolean isAmbiguous() {
        return delegate().isAmbiguous();
    }

    @Override
    public void destroy(T instance) {
        delegate().destroy(instance);
    }

}
