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

package org.jboss.weld.interceptor.util;

import java.util.Iterator;

import org.jboss.weld.interceptor.spi.metadata.MethodMetadata;

/**
 * @author Marius Bogoevici
 */
public abstract class ImmutableIteratorWrapper<T> implements Iterator<MethodMetadata> {

    private final Iterator<T> originalIterator;

    protected ImmutableIteratorWrapper(Iterator<T> originalIterator) {
        this.originalIterator = originalIterator;
    }


    public boolean hasNext() {
        return originalIterator.hasNext();
    }

    public MethodMetadata next() {
        return wrapObject(originalIterator.next());
    }

    protected abstract MethodMetadata wrapObject(T t);

    public void remove() {
        throw new UnsupportedOperationException("Removal not supported");
    }
}
