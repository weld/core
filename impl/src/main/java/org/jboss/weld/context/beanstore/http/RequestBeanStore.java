package org.jboss.weld.context.beanstore.http;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.jboss.weld.context.beanstore.AttributeBeanStore;
import org.jboss.weld.context.beanstore.LockStore;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.servlet.HttpContextLifecycle;
import org.jboss.weld.util.collections.EnumerationIterator;

/**
 * <p>
 * A BeanStore that uses a HTTP request as backing storage.
 * </p>
 * <p/>
 * <p>
 * This class is not threadsafe
 * </p>
 *
 * @author Nicklas Karlsson
 * @author David Allen
 * @author Pete Muir
 */
public class RequestBeanStore extends AttributeBeanStore {

    private final HttpServletRequest request;

    public RequestBeanStore(HttpServletRequest request, NamingScheme namingScheme) {
        super(namingScheme);
        this.request = request;
    }

    @Override
    protected Object getAttribute(String key) {
        return request.getAttribute(key);
    }

    @Override
    protected void removeAttribute(String key) {
        request.removeAttribute(key);
    }

    @Override
    protected Iterator<String> getAttributeNames() {
        return new EnumerationIterator<String>(request.getAttributeNames());
    }

    @Override
    protected void setAttribute(String key, Object instance) {
        request.setAttribute(key, instance);
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public LockStore getLockStore() {
        return null;
    }

    @Override
    protected boolean isStoreSyncDuringAttachRequired() {
        return Boolean.TRUE.equals(request.getAttribute(HttpContextLifecycle.ASYNC_STARTED_ATTR_NAME));
    }

}