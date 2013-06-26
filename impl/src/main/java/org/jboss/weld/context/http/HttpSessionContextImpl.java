package org.jboss.weld.context.http;

import java.lang.annotation.Annotation;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.SessionScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.weld.Container;
import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.SimpleNamingScheme;
import org.jboss.weld.context.beanstore.http.EagerSessionBeanStore;
import org.jboss.weld.context.beanstore.http.LazySessionBeanStore;

public class HttpSessionContextImpl extends AbstractBoundContext<HttpServletRequest> implements HttpSessionContext {

    public static final SimpleNamingScheme NAMING_SCHEME = new SimpleNamingScheme(HttpSessionContext.class.getName());

    private final NamingScheme namingScheme;

    public HttpSessionContextImpl() {
        super(true);
        this.namingScheme = NAMING_SCHEME;
    }

    public boolean associate(HttpServletRequest request) {
        if (getBeanStore() == null) {
            // Don't reassociate
            setBeanStore(new LazySessionBeanStore(request, namingScheme));
            return true;
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
            // We are in a request, invalidate it
            invalidate();
            getConversationContext().destroy(session);
            return false;
        }
    }

    public Class<? extends Annotation> getScope() {
        return SessionScoped.class;
    }

    protected HttpConversationContext getConversationContext() {
        return Container.instance().deploymentManager().instance().select(HttpConversationContext.class).get();
    }

    protected Conversation getConversation() {
        return Container.instance().deploymentManager().instance().select(Conversation.class).get();
    }
}
