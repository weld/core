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
package org.jboss.weld.contexts.beanstore;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.logging.ContextLogger;
import org.jboss.weld.serialization.spi.BeanIdentifier;

/**
 * <p>
 * A bound bean store backed by attributes. This bean store is "write-through" -
 * if attached it will write any modifications to the backing store immediately.
 * If detached modifications will not be written through. If the bean store is
 * reattached, then any local modifications will be written to the underlying
 * store.
 * </p>
 * <p/>
 * <p>
 * This construct is not thread safe.
 * </p>
 *
 * @author Pete Muir
 * @author Nicklas Karlsson
 * @author David Allen
 */
public abstract class AttributeBeanStore implements BoundBeanStore {

    private final HashMapBeanStore beanStore;
    private final NamingScheme namingScheme;

    private final boolean attributeLazyFetchingEnabled;

    private boolean attached;

    /**
     *
     * @param namingScheme
     * @param attributeLazyFetchingEnabled
     */
    public AttributeBeanStore(NamingScheme namingScheme, boolean attributeLazyFetchingEnabled) {
        this.namingScheme = namingScheme;
        this.beanStore = new HashMapBeanStore();
        this.attributeLazyFetchingEnabled = attributeLazyFetchingEnabled;
    }

    /**
     * Detach the bean store, causing updates to longer be written through to the
     * underlying store.
     */
    public boolean detach() {
        if (attached) {
            attached = false;
            ContextLogger.LOG.beanStoreDetached(this);
            return true;
        } else {
            return false;
        }
    }

    /**
     * <p>
     * Attach the bean store, any updates from now on will be written through to
     * the underlying store.
     * </p>
     * <p/>
     * <p>
     * When the bean store is attached, the detached state is assumed to be
     * authoritative if there are any conflicts.
     * </p>
     */
    public boolean attach() {
        if (!attached) {
            attached = true;
            if (isLocalBeanStoreSyncNeeded()) {
                if (!beanStore.delegate().isEmpty()) {
                    // The local bean store is authoritative, so copy everything to the backing store
                    for (BeanIdentifier id : beanStore) {
                        ContextualInstance<?> instance = beanStore.get(id);
                        String prefixedId = getNamingScheme().prefix(id);
                        ContextLogger.LOG.updatingStoreWithContextualUnderId(instance, id);
                        setAttribute(prefixedId, instance);
                    }
                }
                if (!isAttributeLazyFetchingEnabled()) {
                    // Additionally copy anything not in the local bean store but in the backing store
                    fetchUninitializedAttributes();
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Fetch all relevant attributes from the backing store and copy instances which are not present in the local bean store.
     */
    public void fetchUninitializedAttributes() {
        for (String prefixedId : getPrefixedAttributeNames()) {
            BeanIdentifier id = getNamingScheme().deprefix(prefixedId);
            if (!beanStore.contains(id)) {
                ContextualInstance<?> instance = (ContextualInstance<?>) getAttribute(prefixedId);
                beanStore.put(id, instance);
                ContextLogger.LOG.addingDetachedContextualUnderId(instance, id);
            }
        }
    }

    public boolean isAttached() {
        return attached;
    }

    @Override
    public <T> ContextualInstance<T> get(BeanIdentifier id) {
        ContextualInstance<T> instance = beanStore.get(id);
        if (instance == null && isAttached() && isAttributeLazyFetchingEnabled()) {
            instance = cast(getAttribute(namingScheme.prefix(id)));
            if (instance != null) {
                beanStore.put(id, instance);
            }
        }
        ContextLogger.LOG.contextualInstanceFound(id, instance, this);
        return instance;
    }

    @Override
    public <T> void put(BeanIdentifier id, ContextualInstance<T> instance) {
        beanStore.put(id, instance); // moved due to WELD-892
        if (isAttached()) {
            setAttribute(namingScheme.prefix(id), instance);
        }
        ContextLogger.LOG.contextualInstanceAdded(instance.getContextual(), id, this);
    }

    @Override
    public <T> ContextualInstance<T> remove(BeanIdentifier id) {
        ContextualInstance<T> instance = beanStore.remove(id);
        String prefixedId = namingScheme.prefix(id);
        if (instance == null && isAttached() && isAttributeLazyFetchingEnabled()) {
            // If no instance is found and the bean store is attached then attempt to get the attribute from the backing store
            instance = cast(getAttribute(prefixedId));
        }
        if (instance != null) {
            if (isAttached()) {
                removeAttribute(prefixedId);
            }
            ContextLogger.LOG.contextualInstanceRemoved(id, this);
        }
        return instance;
    }

    public void clear() {
        Iterator<BeanIdentifier> it = iterator();
        while (it.hasNext()) {
            BeanIdentifier id = it.next();
            if (isAttached()) {
                String prefixedId = namingScheme.prefix(id);
                removeAttribute(prefixedId);
            }
            it.remove();
            ContextLogger.LOG.contextualInstanceRemoved(id, this);
        }
        ContextLogger.LOG.contextCleared(this);
    }

    public boolean contains(BeanIdentifier id) {
        return get(id) != null;
    }

    protected NamingScheme getNamingScheme() {
        return namingScheme;
    }

    public Iterator<BeanIdentifier> iterator() {
        Iterator<BeanIdentifier> iterator;
        if (isAttributeLazyFetchingEnabled()) {
            // Merge the bean identifiers from the local bean store and the backing store
            Set<BeanIdentifier> identifiers = new HashSet<>();
            for (BeanIdentifier id : beanStore) {
                identifiers.add(id);
            }
            for (String prefixedId : getPrefixedAttributeNames()) {
                identifiers.add(getNamingScheme().deprefix(prefixedId));
            }
            iterator = identifiers.iterator();
        } else {
            iterator = beanStore.iterator();
        }
        return iterator;
    }

    /**
     * Gets an attribute from the underlying storage
     *
     * @param prefixedId The (prefixed) id of the attribute
     * @return The data
     */
    protected abstract Object getAttribute(String prefixedId);

    /**
     * Removes an attribute from the underlying storage
     *
     * @param prefixedId The (prefixed) id of the attribute to remove
     */
    protected abstract void removeAttribute(String prefixedId);

    /**
     * Gets an enumeration of the attribute names present in the underlying
     * storage. The collection must guarantee non-interference with other threads
     * when iterating over it using iterator.
     *
     * @return The attribute names
     */
    protected abstract Iterator<String> getAttributeNames();

    /**
     * Gets an enumeration of the attribute names present in the underlying
     * storage
     *
     * @return The attribute names
     */
    protected Collection<String> getPrefixedAttributeNames() {
        return getNamingScheme().filterIds(getAttributeNames());
    }

    /**
     * Sets an instance under a key in the underlying storage
     *
     * @param prefixedId The (prefixed) id of the attribute to set
     * @param instance The instance
     */
    protected abstract void setAttribute(String prefixedId, Object instance);

    public LockedBean lock(final BeanIdentifier id) {
        LockStore lockStore = getLockStore();
        if (lockStore == null) {
            //if the lockstore is null then no locking is necessary, as the underlying
            //context is single threaded
            return null;
        }
        return lockStore.lock(id);
    }

    protected abstract LockStore getLockStore();

    /**
     *
     * @return <code>true</code> if a bean store synchronization is required during {@link #attach()} invocation,
     *         <code>false</code> otherwise
     */
    protected boolean isLocalBeanStoreSyncNeeded() {
        return true;
    }

    /**
     *
     * @return <code>true</code> if attributes should be fetched lazily, <code>false</code> otherwise
     */
    public boolean isAttributeLazyFetchingEnabled() {
        return attributeLazyFetchingEnabled;
    }
}
