package org.jboss.weld.context.http;

import static org.jboss.weld.logging.Category.CONTEXT;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;

import org.jboss.weld.Container;
import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.context.ManagedConversation;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.SimpleNamingScheme;
import org.jboss.weld.context.beanstore.http.EagerSessionBeanStore;
import org.jboss.weld.context.beanstore.http.LazySessionBeanStore;
import org.jboss.weld.logging.messages.ContextMessage;
import org.slf4j.cal10n.LocLogger;

import javax.enterprise.context.SessionScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.lang.annotation.Annotation;

public class HttpSessionContextImpl extends AbstractBoundContext<HttpServletRequest> implements HttpSessionContext {

    private static final LocLogger log = loggerFactory().getLogger(CONTEXT);

    private final NamingScheme namingScheme;

    public HttpSessionContextImpl() {
        super(true);
        this.namingScheme = new SimpleNamingScheme(HttpSessionContext.class.getName());
    }

    public boolean associate(HttpServletRequest request) {
        // At this point the bean store should never be set - see also WeldListener.nestedInvocationGuard
        if (getBeanStore() != null) {
            log.warn(ContextMessage.BEAN_STORE_LEAK_DURING_ASSOCIATION, this.getClass().getName(), request);
        }
        // We always associate a new bean store to avoid possible leaks (security threats)
        setBeanStore(new LazySessionBeanStore(request, namingScheme));
        return true;
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
        return Container.instance().deploymentManager().instance().select(HttpConversationContext.class).get();
    }

}
