package org.jboss.weld.context.http;

import java.lang.annotation.Annotation;

import javax.enterprise.context.SessionScoped;
import javax.servlet.http.HttpSession;

import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.context.beanstore.http.EagerSessionBeanStore;

/**
 * This special http session context is necessary because HttpSessionListeners that are called when a session
 * is being destroyed outside the scope of a HTTP request, need to be able to access the session context.
 * We can't simply activate the regular HttpSessionContext, since we would need an HttpServletRequest to associate
 * and activate the context.
 *
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class HttpSessionDestructionContext extends AbstractBoundContext<HttpSession> {


    public HttpSessionDestructionContext() {
        super(true);
    }

    @Override
    public boolean associate(HttpSession session) {
        if (getBeanStore() == null) {
            // Don't reassociate
            setBeanStore(new EagerSessionBeanStore(HttpSessionContextImpl.NAMING_SCHEME, session));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return SessionScoped.class;
    }
}
