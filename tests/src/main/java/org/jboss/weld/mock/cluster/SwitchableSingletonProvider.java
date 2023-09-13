/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.mock.cluster;

import java.util.Hashtable;
import java.util.Map;

import org.jboss.weld.bootstrap.api.Singleton;
import org.jboss.weld.bootstrap.api.SingletonProvider;

public class SwitchableSingletonProvider extends SingletonProvider {

    public static void use(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        SwitchableSingleton.id = id;
    }

    private static class SwitchableSingleton<T> implements Singleton<T> {

        private static Integer id = 0;

        private final Map<Integer, T> store;

        public SwitchableSingleton() {
            this.store = new Hashtable<Integer, T>();
        }

        public void clear(String ident) {
            store.remove(id);
        }

        public T get(String ident) {
            return store.get(id);
        }

        public boolean isSet(String ident) {
            return store.containsKey(id);
        }

        public void set(String ident, T object) {
            store.put(id, object);
        }

    }

    @Override
    public <T> Singleton<T> create(Class<? extends T> expectedType) {
        return new SwitchableSingleton<T>();
    }

}
