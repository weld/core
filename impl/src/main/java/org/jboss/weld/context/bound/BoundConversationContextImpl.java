package org.jboss.weld.context.bound;

import org.jboss.weld.context.AbstractConversationContext;
import org.jboss.weld.context.beanstore.BoundBeanStore;
import org.jboss.weld.context.beanstore.MapBeanStore;
import org.jboss.weld.context.beanstore.NamingScheme;

import java.util.Map;

public class BoundConversationContextImpl extends AbstractConversationContext<BoundRequest, Map<String, Object>> implements BoundConversationContext {

    public BoundConversationContextImpl(String contextId) {
        super(contextId);
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
        return new MapBeanStore(namingScheme, request.getSessionMap(false));
    }

    @Override
    protected BoundBeanStore createSessionBeanStore(NamingScheme namingScheme, Map<String, Object> session) {
        return new MapBeanStore(namingScheme, session);
    }

    @Override
    protected Object getSessionAttributeFromSession(Map<String, Object> session, String name) {
        return session.get(name);
    }

    @Override
    protected Map<String, Object> getSessionFromRequest(BoundRequest request, boolean create) {
        return request.getSessionMap(create);
    }

}
