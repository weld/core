package org.jboss.weld.module.web.context.beanstore.http;

import javax.servlet.http.HttpSession;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.contexts.beanstore.NamingScheme;
import org.jboss.weld.logging.ContextLogger;

/**
 * <p>
 * A BeanStore that uses a HTTP session as backing storage. This bean store is
 * automatically attached when created.
 * </p>
 * <p/>
 * <p>
 * This bean store is backed by an HttpSession directly. If you want a bean
 * store that only requires session creation when an instance must be written,
 * use {@link LazySessionBeanStore}.
 * </p>
 * <p/>
 * <p>
 * This class is not threadsafe
 * </p>
 *
 * @author Nicklas Karlsson
 * @author David Allen
 * @author Pete Muir
 * @see LazySessionBeanStore
 */
public class EagerSessionBeanStore extends AbstractSessionBeanStore {

    private final HttpSession session;

    public EagerSessionBeanStore(NamingScheme namingScheme, HttpSession session, ServiceRegistry serviceRegistry) {
        super(namingScheme, false, serviceRegistry);
        this.session = session;
        ContextLogger.LOG.loadingBeanStoreMapFromSession(this, getSession(false));
    }

    @Override
    protected HttpSession getSession(boolean create) {
        return session;
    }

}
