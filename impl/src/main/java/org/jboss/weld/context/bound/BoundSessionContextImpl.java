package org.jboss.weld.context.bound;

import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.context.beanstore.MapBeanStore;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.SimpleNamingScheme;

import javax.enterprise.context.SessionScoped;
import java.lang.annotation.Annotation;
import java.util.Map;

public class BoundSessionContextImpl extends AbstractBoundContext<Map<String, Object>> implements BoundSessionContext {

    private static final String IDENTIFIER = BoundSessionContextImpl.class.getName();

    private final NamingScheme namingScheme;

    public BoundSessionContextImpl(String contextId) {
        super(contextId, false);
        this.namingScheme = new SimpleNamingScheme(BoundSessionContext.class.getName());
    }

    public Class<? extends Annotation> getScope() {
        return SessionScoped.class;
    }

    public boolean associate(Map<String, Object> storage) {
        if (getBeanStore() == null) {
            storage.put(IDENTIFIER, IDENTIFIER);
            setBeanStore(new MapBeanStore(namingScheme, storage));
            return true;
        } else {
            return false;
        }
    }

    public boolean dissociate(Map<String, Object> storage) {
        if (storage.containsKey(IDENTIFIER)) {
            try {
                storage.remove(IDENTIFIER);
                setBeanStore(null);
                return true;
            } finally {
                cleanup();
            }

        } else {
            return false;
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

}
