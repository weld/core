package org.jboss.weld.context.http;

import org.jboss.weld.context.AbstractConversationContext;
import org.jboss.weld.context.beanstore.BoundBeanStore;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.http.EagerSessionBeanStore;
import org.jboss.weld.context.beanstore.http.LazySessionBeanStore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class HttpConversationContextImpl extends AbstractConversationContext<HttpServletRequest, HttpSession> implements HttpConversationContext {

    public HttpConversationContextImpl(String contextId) {
        super(contextId);
    }

    @Override
    protected void setSessionAttribute(HttpServletRequest request, String name, Object value, boolean create) {
        if (create || request.getSession(false) != null) {
            request.getSession(true).setAttribute(name, value);
        }
    }

    @Override
    protected Object getSessionAttribute(HttpServletRequest request, String name, boolean create) {
        if (create || request.getSession(false) != null) {
            return request.getSession(true).getAttribute(name);
        } else {
            return null;
        }
    }

    @Override
    protected void removeRequestAttribute(HttpServletRequest request, String name) {
        request.removeAttribute(name);
    }

    @Override
    protected void setRequestAttribute(HttpServletRequest request, String name, Object value) {
        request.setAttribute(name, value);
    }

    @Override
    protected Object getRequestAttribute(HttpServletRequest request, String name) {
        return request.getAttribute(name);
    }

    @Override
    protected BoundBeanStore createRequestBeanStore(NamingScheme namingScheme, HttpServletRequest request) {
        return new LazySessionBeanStore(request, namingScheme);
    }

    @Override
    protected BoundBeanStore createSessionBeanStore(NamingScheme namingScheme, HttpSession session) {
        return new EagerSessionBeanStore(namingScheme, session);
    }

    @Override
    protected Object getSessionAttributeFromSession(HttpSession session, String name) {
        return session.getAttribute(name);
    }

    @Override
    protected HttpSession getSessionFromRequest(HttpServletRequest request, boolean create) {
        return request.getSession(create);
    }


}
