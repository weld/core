package org.jboss.weld.context.http;

import java.lang.annotation.Annotation;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.SessionScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.weld.Container;
import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.SimpleBeanIdentifierIndexNamingScheme;
import org.jboss.weld.context.beanstore.http.EagerSessionBeanStore;
import org.jboss.weld.context.beanstore.http.LazySessionBeanStore;
import org.jboss.weld.logging.ContextLogger;
import org.jboss.weld.serialization.BeanIdentifierIndex;

public class HttpSessionContextImpl extends AbstractBoundContext<HttpServletRequest> implements HttpSessionContext {

    // There is no need to store FQCN in a session key
    static final String NAMING_SCHEME_PREFIX = "WELD_S";

    static final String KEY_BEAN_ID_INDEX_HASH = NAMING_SCHEME_PREFIX + "_HASH";

    private final NamingScheme namingScheme;
    private final String contextId;

    public HttpSessionContextImpl(String contextId, BeanIdentifierIndex index) {
        super(contextId, true);
        this.namingScheme = new SimpleBeanIdentifierIndexNamingScheme(NAMING_SCHEME_PREFIX, index);
        this.contextId = contextId;
    }

    public boolean associate(HttpServletRequest request) {
        // At this point the bean store should never be set - see also HttpContextLifecycle.nestedInvocationGuard
        if (getBeanStore() != null) {
            ContextLogger.LOG.beanStoreLeakDuringAssociation(this.getClass().getName(), request);
        }
        // We always associate a new bean store to avoid possible leaks (security threats)
        setBeanStore(new LazySessionBeanStore(request, namingScheme));
        checkBeanIdentifierIndexConsistency(request);
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

    private void checkBeanIdentifierIndexConsistency(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            BeanIdentifierIndex index = getServiceRegistry().get(BeanIdentifierIndex.class);
            if (index != null && index.isBuilt()) {
                Object hash = session.getAttribute(KEY_BEAN_ID_INDEX_HASH);
                if (hash != null) {
                    if (!index.getIndexHash().equals(hash)) {
                        throw ContextLogger.LOG.beanIdentifierIndexInconsistencyDetected();
                    }
                } else {
                    session.setAttribute(KEY_BEAN_ID_INDEX_HASH, index.getIndexHash());
                }
            }
        }
    }

}
