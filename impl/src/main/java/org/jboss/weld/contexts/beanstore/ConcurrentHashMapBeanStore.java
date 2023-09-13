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

package org.jboss.weld.contexts.beanstore;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.weld.serialization.spi.BeanIdentifier;

/**
 * A BeanStore that uses a HashMap as backing storage
 *
 * @author Nicklas Karlsson
 */
public class ConcurrentHashMapBeanStore extends AbstractMapBackedBeanStore implements Serializable {

    private static final long serialVersionUID = 4770689245633688471L;

    // The backing map
    protected Map<BeanIdentifier, Object> delegate;
    private transient volatile LockStore lockStore;

    /**
     * Constructor
     */
    public ConcurrentHashMapBeanStore() {
        delegate = new ConcurrentHashMap<BeanIdentifier, Object>();
    }

    /**
     * Gets the delegate for the store
     *
     * @return The delegate
     */
    @Override
    public Map<BeanIdentifier, Object> delegate() {
        return delegate;
    }

    @Override
    public String toString() {
        return "contextuals " + delegate;
    }

    public LockedBean lock(final BeanIdentifier id) {
        LockStore lockStore = this.lockStore;
        if (lockStore == null) {
            synchronized (this) {
                lockStore = this.lockStore;
                if (lockStore == null) {
                    this.lockStore = lockStore = new LockStore();
                }
            }
        }
        return lockStore.lock(id);
    }
}
