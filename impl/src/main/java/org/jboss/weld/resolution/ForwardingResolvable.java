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
package org.jboss.weld.resolution;

import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

public abstract class ForwardingResolvable implements Resolvable {

    protected abstract Resolvable delegate();

    @Override
    public Set<QualifierInstance> getQualifiers() {
        return delegate().getQualifiers();
    }

    @Override
    public Set<Type> getTypes() {
        return delegate().getTypes();
    }

    @Override
    public Class<?> getJavaClass() {
        return delegate().getJavaClass();
    }

    @Override
    public Bean<?> getDeclaringBean() {
        return delegate().getDeclaringBean();
    }

    @Override
    public boolean isDelegate() {
        return delegate().isDelegate();
    }

    @Override
    public boolean equals(Object obj) {
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
