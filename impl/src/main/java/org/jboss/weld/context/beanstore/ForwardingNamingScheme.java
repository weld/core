/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.context.beanstore;

import java.util.Collection;
import java.util.Iterator;

import org.jboss.weld.serialization.spi.BeanIdentifier;

public abstract class ForwardingNamingScheme implements NamingScheme {

    protected abstract NamingScheme delegate();

    @Override
    public boolean accept(String id) {
        return delegate().accept(id);
    }

    @Override
    public BeanIdentifier deprefix(String id) {
        return delegate().deprefix(id);
    }

    @Override
    public String prefix(BeanIdentifier id) {
        return delegate().prefix(id);
    }

    @Override
    public Collection<String> filterIds(Iterator<String> ids) {
        return delegate().filterIds(ids);
    }

    @Override
    public Collection<BeanIdentifier> deprefix(Collection<String> ids) {
        return delegate().deprefix(ids);
    }

    @Override
    public Collection<String> prefix(Collection<BeanIdentifier> ids) {
        return delegate().prefix(ids);
    }

    @Override
    public boolean equals(Object obj) {
        return delegate().equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    public String toString() {
        return delegate().toString();
    }

}
