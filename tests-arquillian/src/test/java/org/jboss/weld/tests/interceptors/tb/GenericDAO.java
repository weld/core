/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.interceptors.tb;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Tx
@ApplicationScoped
public abstract class GenericDAO<T extends Entity> implements DAO<T> {
    abstract Class<T> entityClass();

    public void save(T t) {
    }

    @Tx(1)
    public T find(Long id) {
        return find(entityClass(), id);
    }

    @Tx(1)
    public <U> U find(Class<U> clazz, Long id) {
        if (clazz == null)
            throw new IllegalArgumentException("Null clazz");
        if (id == null)
            throw new IllegalArgumentException("Null id");

        Object marker = TxInterceptor.clients.get();
        if (marker == null)
            throw new IllegalArgumentException("No Tx marker!");

        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
