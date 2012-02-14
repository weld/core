package org.jboss.weld.bean.proxy;

/**
 * The interface implemented by proxy classes.
 *
 * @see ProxyFactory
 */
public interface ProxyObject {
    /**
     * Sets a handler.  It can be used for changing handlers
     * during runtime.
     */
    void setHandler(MethodHandler mi);

    /**
     * Get the handler.
     * This can be used to access values of the underlying MethodHandler
     * or to serialize it properly.
     */
    MethodHandler getHandler();
}
