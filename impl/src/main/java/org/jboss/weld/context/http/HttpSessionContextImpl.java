package org.jboss.weld.context.http;

import java.lang.annotation.Annotation;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.SessionScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.weld.Container;
import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.context.beanstore.BeanIdentifierIndexNamingScheme;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.http.EagerSessionBeanStore;
import org.jboss.weld.context.beanstore.http.LazySessionBeanStore;
import org.jboss.weld.serialization.BeanIdentifierIndex;

public class HttpSessionContextImpl extends AbstractBoundContext<HttpServletRequest> implements HttpSessionContext {

    // There is no need to store FQCN in a session key
    static final String INDEX_NAMING_SCHEME_PREFIX = "WELD_S";

    private final NamingScheme namingScheme;
    private final String contextId;

    public HttpSessionContextImpl(String contextId, BeanIdentifierIndex index) {
        super(contextId, true);
        this.namingScheme = new BeanIdentifierIndexNamingScheme(INDEX_NAMING_SCHEME_PREFIX, index);
        this.contextId = contextId;
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
        return Container.instance(contextId).deploymentManager().instance().select(HttpConversationContext.class).get();
    }

    protected Conversation getConversation() {
        return Container.instance(contextId).deploymentManager().instance().select(Conversation.class).get();
    }
}
