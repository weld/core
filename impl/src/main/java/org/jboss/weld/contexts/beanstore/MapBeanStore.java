package org.jboss.weld.contexts.beanstore;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class MapBeanStore extends AttributeBeanStore {

    protected transient volatile LockStore lockStore;

    private final Map<String, Object> delegate;
    /*
     * Indicates whether it is safe to iterate over the keys if the delegate without synchronizing on it.
     * For example, it is safe for ConcurrentHashMap but is not for Collections.synchronizedMap()
     */
    private final boolean safeIteration;

    public MapBeanStore(NamingScheme namingScheme, Map<String, Object> delegate) {
        this(namingScheme, delegate, false);
    }

    public MapBeanStore(NamingScheme namingScheme, Map<String, Object> delegate, boolean safeIteration) {
        super(namingScheme, false);
        this.delegate = delegate;
        this.safeIteration = safeIteration;
    }

    @Override
    protected Object getAttribute(String prefixedId) {
        return delegate.get(prefixedId);
    }

    @Override
    protected void removeAttribute(String prefixedId) {
        delegate.remove(prefixedId);
    }

    @Override
    protected Iterator<String> getAttributeNames() {
        if (safeIteration) {
            return new HashSet<String>(delegate.keySet()).iterator();
        }
        synchronized (delegate) {
            return new HashSet<String>(delegate.keySet()).iterator();
        }
    }

    @Override
    protected void setAttribute(String prefixedId, Object instance) {
        delegate.put(prefixedId, instance);
    }

    public LockStore getLockStore() {
        LockStore lockStore = this.lockStore;
        if (lockStore == null) {
            synchronized (this) {
                lockStore = this.lockStore;
                if (lockStore == null) {
                    this.lockStore = lockStore = new LockStore();
                }
            }
        }
        return lockStore;
    }
}
