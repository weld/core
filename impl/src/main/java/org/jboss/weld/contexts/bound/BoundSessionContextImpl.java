package org.jboss.weld.contexts.bound;

import java.lang.annotation.Annotation;
import java.util.Map;

import jakarta.enterprise.context.SessionScoped;

import org.jboss.weld.context.bound.BoundSessionContext;
import org.jboss.weld.contexts.AbstractBoundContext;
import org.jboss.weld.contexts.beanstore.NamingScheme;
import org.jboss.weld.contexts.beanstore.SessionMapBeanStore;
import org.jboss.weld.contexts.beanstore.SimpleBeanIdentifierIndexNamingScheme;
import org.jboss.weld.logging.ContextLogger;
import org.jboss.weld.serialization.BeanIdentifierIndex;

public class BoundSessionContextImpl extends AbstractBoundContext<Map<String, Object>> implements BoundSessionContext {

    // There is no need to store FQCN in a session key
    static final String NAMING_SCHEME_PREFIX = "WELD_BS";

    static final String KEY_BEAN_ID_INDEX_HASH = NAMING_SCHEME_PREFIX + "_HASH";

    private final NamingScheme namingScheme;

    public BoundSessionContextImpl(String contextId, BeanIdentifierIndex index) {
        super(contextId, true);
        this.namingScheme = new SimpleBeanIdentifierIndexNamingScheme(NAMING_SCHEME_PREFIX, index);
    }

    public Class<? extends Annotation> getScope() {
        return SessionScoped.class;
    }

    public boolean associate(Map<String, Object> storage) {
        if (getBeanStore() == null) {
            setBeanStore(new SessionMapBeanStore(namingScheme, storage));
            checkBeanIdentifierIndexConsistency(storage);
            return true;
        } else {
            return false;
        }
    }

    private void checkBeanIdentifierIndexConsistency(Map<String, Object> storage) {
        BeanIdentifierIndex index = getServiceRegistry().get(BeanIdentifierIndex.class);
        if (index != null && index.isBuilt()) {
            Object hash = storage.get(KEY_BEAN_ID_INDEX_HASH);
            if (hash != null) {
                if (!index.getIndexHash().equals(hash)) {
                    throw ContextLogger.LOG.beanIdentifierIndexInconsistencyDetected(hash.toString(), index.getDebugInfo());
                }
            } else {
                storage.put(KEY_BEAN_ID_INDEX_HASH, index.getIndexHash());
            }
        }
    }
}
