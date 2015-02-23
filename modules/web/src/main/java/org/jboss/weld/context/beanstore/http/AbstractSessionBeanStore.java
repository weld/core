package org.jboss.weld.context.beanstore.http;

import static java.util.Collections.emptyList;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.context.beanstore.AttributeBeanStore;
import org.jboss.weld.context.beanstore.LockStore;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.logging.ContextLogger;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.util.collections.EnumerationList;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Base class providing an HttpSession backed, bound bean store.
 *
 * @author Pete Muir
 * @author David Allen
 * @author Nicklas Karlsson
 * @see LazySessionBeanStore
 * @see EagerSessionBeanStore
 */
public abstract class AbstractSessionBeanStore extends AttributeBeanStore {

    private static final String SESSION_KEY = "org.jboss.weld.context.beanstore.http.LockStore";

    private transient volatile LockStore lockStore;

    private static final ThreadLocal<LockStore> CURRENT_LOCK_STORE = new ThreadLocal<LockStore>();

    protected abstract HttpSession getSession(boolean create);

    public AbstractSessionBeanStore(NamingScheme namingScheme) {
        super(namingScheme);
    }

    protected Collection<String> getAttributeNames() {
        HttpSession session = getSession(false);
        if (session == null) {
            return emptyList();
        } else {
            return new EnumerationList<>(Reflections.<Enumeration<String>>cast(session.getAttributeNames()));
        }
    }

    @Override
    protected void removeAttribute(String key) {
        HttpSession session = getSession(false);
        if (session != null) {
            session.removeAttribute(key);
            ContextLogger.LOG.removedKeyFromSession(key, this.getSession(false).getId());
        } else {
            ContextLogger.LOG.unableToRemoveKeyFromSession(key);
        }
    }

    @Override
    protected void setAttribute(String key, Object instance) {
        HttpSession session = getSession(true);
        if (session != null) {
            session.setAttribute(key, instance);
            ContextLogger.LOG.addedKeyToSession(key, this.getSession(false).getId());
        } else {
            ContextLogger.LOG.unableToAddKeyToSession(key);
        }
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
    protected Object getAttribute(String prefixedId) {
        HttpSession session = getSession(false);
        if (session != null) {
            return session.getAttribute(prefixedId);
        }
        return null;
    }

    @Override
    protected LockStore getLockStore() {
        LockStore lockStore = this.lockStore;
        if (lockStore == null) {
            //needed to prevent some edge cases
            //where we would otherwise enter an infinite loop
            lockStore = CURRENT_LOCK_STORE.get();
            if(lockStore != null) {
                return lockStore;
            }
            HttpSession session = getSession(false);
            if(session == null) {
                lockStore = new LockStore();
                CURRENT_LOCK_STORE.set(lockStore);
                try {
                session = getSession(true);
                } finally {
                    CURRENT_LOCK_STORE.remove();
                }
            }
            lockStore = (LockStore) session.getAttribute(SESSION_KEY);
            if (lockStore == null) {
                //we don't really have anything we can lock on
                //so we just acquire a big global lock
                //this should only be taken on session creation though
                //so should not be a problem
                synchronized (AbstractSessionBeanStore.class) {
                    lockStore = (LockStore) session.getAttribute(SESSION_KEY);
                    if (lockStore == null) {
                        lockStore = new LockStore();
                        session.setAttribute(SESSION_KEY, lockStore);
                    }
                }
            }
            this.lockStore = lockStore;
        }
        return lockStore;
    }
}
