/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.contexts.beanstore;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.serialization.spi.BeanIdentifier;

/**
 * A {@link MapBeanStore} whose {@link LockStore} is associated with the key {@value #LOCK_STORE_KEY} in the provided map and so
 * may be shared across several
 * stores used within a single "session". Moreover {@link BeanStore#get(BeanIdentifier)} not only searches the cached bean store
 * in {@link AttributeBeanStore}
 * but also the underlying map.
 *
 * @author Martin Kouba
 * @see WELD-1575
 */
public class SessionMapBeanStore extends MapBeanStore {

    private static final String LOCK_STORE_KEY = "org.jboss.weld.context.beanstore.LockStore";

    public SessionMapBeanStore(NamingScheme namingScheme, Map<String, Object> delegate) {
        super(namingScheme, delegate, delegate instanceof ConcurrentHashMap);
    }

    @Override
    public <T> ContextualInstance<T> get(BeanIdentifier id) {
        ContextualInstance<T> instance = super.get(id);
        if (instance == null && isAttached()) {
            String prefixedId = getNamingScheme().prefix(id);
            instance = cast(getAttribute(prefixedId));
        }
        return instance;
    }

    @Override
    public LockStore getLockStore() {
        LockStore lockStore = this.lockStore;
        if (lockStore == null) {
            lockStore = (LockStore) getAttribute(LOCK_STORE_KEY);
            if (lockStore == null) {
                synchronized (SessionMapBeanStore.class) {
                    lockStore = (LockStore) getAttribute(LOCK_STORE_KEY);
                    if (lockStore == null) {
                        lockStore = new LockStore();
                        setAttribute(LOCK_STORE_KEY, lockStore);
                    }
                }
            }
            this.lockStore = lockStore;
        }
        return lockStore;
    }

}
