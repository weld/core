package org.jboss.weld.context.beanstore.http;

import org.jboss.weld.context.beanstore.NamingScheme;
import org.slf4j.cal10n.LocLogger;

import javax.servlet.http.HttpSession;

import static org.jboss.weld.logging.Category.CONTEXT;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;

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
    private static final LocLogger log = loggerFactory().getLogger(CONTEXT);

    private final HttpSession session;

    public EagerSessionBeanStore(NamingScheme namingScheme, HttpSession session) {
        super(namingScheme);
        this.session = session;
        log.trace("Loading bean store " + this + " map from session " + getSession(false));
    }

    @Override
    protected HttpSession getSession(boolean create) {
        return session;
    }

}
