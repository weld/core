package org.jboss.weld.contexts.bound;

import java.util.Iterator;
import java.util.Map;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.BoundRequest;
import org.jboss.weld.contexts.AbstractConversationContext;
import org.jboss.weld.contexts.beanstore.BoundBeanStore;
import org.jboss.weld.contexts.beanstore.NamingScheme;
import org.jboss.weld.contexts.beanstore.SessionMapBeanStore;

public class BoundConversationContextImpl extends AbstractConversationContext<BoundRequest, Map<String, Object>>
        implements BoundConversationContext {

    // There is no need to store FQCN in a session key
    private static final String NAMING_SCHEME_PREFIX = "WELD_BC";

    public BoundConversationContextImpl(String contextId, ServiceRegistry services) {
        super(contextId, services);
    }

    @Override
    protected void setSessionAttribute(BoundRequest request, String name, Object value, boolean create) {
        request.getSessionMap(create).put(name, value);
    }

    @Override
    protected Object getSessionAttribute(BoundRequest request, String name, boolean create) {
        return request.getSessionMap(create).get(name);
    }

    @Override
    protected void removeRequestAttribute(BoundRequest request, String name) {
        request.getRequestMap().remove(name);
    }

    @Override
    protected void setRequestAttribute(BoundRequest request, String name, Object value) {
        request.getRequestMap().put(name, value);
    }

    @Override
    protected Object getRequestAttribute(BoundRequest request, String name) {
        return request.getRequestMap().get(name);
    }

    @Override
    protected BoundBeanStore createRequestBeanStore(NamingScheme namingScheme, BoundRequest request) {
        return new SessionMapBeanStore(namingScheme, request.getSessionMap(false));
    }

    @Override
    protected BoundBeanStore createSessionBeanStore(NamingScheme namingScheme, Map<String, Object> session) {
        return new SessionMapBeanStore(namingScheme, session);
    }

    @Override
    protected Object getSessionAttributeFromSession(Map<String, Object> session, String name) {
        return session.get(name);
    }

    @Override
    protected Map<String, Object> getSessionFromRequest(BoundRequest request, boolean create) {
        return request.getSessionMap(create);
    }

    @Override
    protected String getNamingSchemePrefix() {
        return NAMING_SCHEME_PREFIX;
    }

    @Override
    protected Iterator<String> getSessionAttributeNames(Map<String, Object> session) {
        return session.keySet().iterator();
    }

}
