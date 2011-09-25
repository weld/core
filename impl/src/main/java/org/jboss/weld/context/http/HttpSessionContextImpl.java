package org.jboss.weld.context.http;

import org.jboss.weld.Container;
import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.context.ManagedConversation;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.SimpleNamingScheme;
import org.jboss.weld.context.beanstore.http.EagerSessionBeanStore;
import org.jboss.weld.context.beanstore.http.LazySessionBeanStore;

import javax.enterprise.context.SessionScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.annotation.Annotation;

public class HttpSessionContextImpl extends AbstractBoundContext<HttpServletRequest> implements HttpSessionContext {

    private static final String IDENTIFIER = HttpSessionContextImpl.class.getName();

    private final NamingScheme namingScheme;

    public HttpSessionContextImpl(String contextId) {
        super(contextId, true);
        this.namingScheme = new SimpleNamingScheme(HttpSessionContext.class.getName());
    }

    public boolean associate(HttpServletRequest request) {
        if (request.getAttribute(IDENTIFIER) == null) {
            // Don't reassociate
            request.setAttribute(IDENTIFIER, IDENTIFIER);
            setBeanStore(new LazySessionBeanStore(request, namingScheme));
            return true;
        } else {
            return false;
        }
    }

    public boolean dissociate(HttpServletRequest request) {

        if (request.getAttribute(IDENTIFIER) != null) {
            try {
                setBeanStore(null);
                request.removeAttribute(IDENTIFIER);
                return true;
            } finally {
                cleanup();
            }
        } else {
            return false;
        }

    }

    public boolean destroy(HttpSession session) {
        if (getBeanStore() == null) {
            try {
                HttpConversationContext conversationContext = getConversationContext();
                setBeanStore(new EagerSessionBeanStore(namingScheme, session));
                activate();
                invalidate();
                conversationContext.destroy(session);
                deactivate();
                setBeanStore(null);
                return true;
            } finally {
                cleanup();
            }
        } else {
            HttpConversationContext conversationContext = getConversationContext();
            // We are in a request, invalidate it
            invalidate();
            if (conversationContext.isActive()) {
                // Make sure *every* conversation is transient so we don't propagate it
                for (ManagedConversation conversation : conversationContext.getConversations()) {
                    if (!conversation.isTransient()) {
                        conversation.end();
                    }
                }
            } else {
                // In a request, with no conversations, so destroy now
                getConversationContext().destroy(session);
            }
            return false;
        }
    }

    public Class<? extends Annotation> getScope() {
        return SessionScoped.class;
    }

    protected HttpConversationContext getConversationContext() {
        return Container.instance(getContextId()).deploymentManager().instance().select(HttpConversationContext.class).get();
    }

}
