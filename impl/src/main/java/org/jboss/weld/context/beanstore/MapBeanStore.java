package org.jboss.weld.context.beanstore;

import java.util.Collection;
import java.util.Map;

public class MapBeanStore extends AttributeBeanStore {

    private final Map<String, Object> delegate;
    private transient volatile LockStore lockStore = new LockStore();

    public MapBeanStore(NamingScheme namingScheme, Map<String, Object> delegate) {
        super(namingScheme);
        this.delegate = delegate;
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
    protected Collection<String> getAttributeNames() {
        return delegate.keySet();
    }

    @Override
    protected void setAttribute(String prefixedId, Object instance) {
        delegate.put(prefixedId, instance);
    }

    public LockStore getLockStore() {
        LockStore lockStore = this.lockStore;
        if(lockStore == null) {
            synchronized (this) {
                lockStore = this.lockStore;
                if(lockStore == null) {
                    this.lockStore = lockStore = new LockStore();
                }
            }
        }
        return lockStore;
    }
}
