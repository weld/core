package org.jboss.weld.context.bound;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.enterprise.context.SessionScoped;

import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.context.beanstore.MapBeanStore;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.SimpleBeanIdentifierIndexNamingScheme;
import org.jboss.weld.serialization.BeanIdentifierIndex;

public class BoundSessionContextImpl extends AbstractBoundContext<Map<String, Object>> implements BoundSessionContext {

    // There is no need to store FQCN in a session key
    static final String NAMING_SCHEME_PREFIX = "WELD_BS";

    private final NamingScheme namingScheme;

    public BoundSessionContextImpl(String contextId, BeanIdentifierIndex index) {
        super(contextId, false);
        this.namingScheme = new SimpleBeanIdentifierIndexNamingScheme(NAMING_SCHEME_PREFIX, index);
    }

    public Class<? extends Annotation> getScope() {
        return SessionScoped.class;
    }

    public boolean associate(Map<String, Object> storage) {
        if (getBeanStore() == null) {
            setBeanStore(new MapBeanStore(namingScheme, storage));
            return true;
        } else {
            return false;
        }
    }
}
