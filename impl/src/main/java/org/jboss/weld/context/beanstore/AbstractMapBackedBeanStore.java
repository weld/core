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
package org.jboss.weld.context.beanstore;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.context.api.ContextualInstance;

import static org.jboss.weld.util.reflection.Reflections.cast;

public abstract class AbstractMapBackedBeanStore implements BeanStore {

    protected abstract Map<String, Object> delegate();
    private transient volatile LockStore lockStore;

    public <T> ContextualInstance<T> get(String id) {
        return cast(delegate().get(id));
    }

    public void clear() {
        delegate().clear();
    }

    public boolean contains(String id) {
        return delegate().containsKey(id);
    }

    public void remove(String id) {
        delegate().remove(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractMapBackedBeanStore) {
            AbstractMapBackedBeanStore that = (AbstractMapBackedBeanStore) obj;
            return this.delegate().equals(that.delegate());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    public Set<String> getContextualIds() {
        return delegate().keySet();
    }

    public <T> void put(String id, ContextualInstance<T> beanInstance) {
        delegate().put(id, beanInstance);
    }

    @Override
    public String toString() {
        return "holding " + delegate().size() + " instances";
    }

    public Iterator<String> iterator() {
        return delegate().keySet().iterator();
    }

    public LockedBean lock(final String id) {
        LockStore lockStore = this.lockStore;
        if(lockStore == null) {
            synchronized (this) {
                lockStore = this.lockStore;
                if(lockStore == null) {
                    this.lockStore = lockStore = new LockStore();
                }
            }
        }
        return lockStore.lock(id);
    }
}
