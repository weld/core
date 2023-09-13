package org.jboss.weld.module.web.context.http;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.SessionScoped;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.jboss.weld.Container;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.context.http.HttpConversationContext;
import org.jboss.weld.context.http.HttpSessionContext;
import org.jboss.weld.contexts.AbstractBoundContext;
import org.jboss.weld.contexts.beanstore.AttributeBeanStore;
import org.jboss.weld.contexts.beanstore.BoundBeanStore;
import org.jboss.weld.contexts.beanstore.NamingScheme;
import org.jboss.weld.contexts.beanstore.SimpleBeanIdentifierIndexNamingScheme;
import org.jboss.weld.logging.ContextLogger;
import org.jboss.weld.module.web.context.beanstore.http.EagerSessionBeanStore;
import org.jboss.weld.module.web.context.beanstore.http.LazySessionBeanStore;
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
        setBeanStore(new LazySessionBeanStore(request, namingScheme,
                getServiceRegistry().getRequired(WeldConfiguration.class).getBooleanProperty(
                        ConfigurationKey.CONTEXT_ATTRIBUTES_LAZY_FETCH),
                getServiceRegistry()));
        checkBeanIdentifierIndexConsistency(request);
        return true;
    }

    public boolean destroy(HttpSession session) {
        final BoundBeanStore beanStore = getBeanStore();
        if (beanStore == null) {
            try {
                HttpConversationContext conversationContext = getConversationContext();
                setBeanStore(new EagerSessionBeanStore(namingScheme, session, getServiceRegistry()));
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
            if (beanStore instanceof AttributeBeanStore) {
                AttributeBeanStore attributeBeanStore = ((AttributeBeanStore) beanStore);
                if (attributeBeanStore.isAttributeLazyFetchingEnabled()) {
                    // At this moment we have to sync the local bean store and the backing store
                    attributeBeanStore.fetchUninitializedAttributes();
                }
            }
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
                        throw ContextLogger.LOG.beanIdentifierIndexInconsistencyDetected(hash.toString(), index.getDebugInfo());
                    }
                } else {
                    // Skip if bean index is empty
                    if (!index.isEmpty()) {
                        session.setAttribute(KEY_BEAN_ID_INDEX_HASH, index.getIndexHash());
                    }
                }
            }
        }
    }

}
