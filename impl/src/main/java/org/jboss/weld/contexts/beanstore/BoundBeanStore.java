package org.jboss.weld.contexts.beanstore;

import org.jboss.weld.context.BoundContext;

/**
 * <p>
 * A bean store may be bound to some external data store, for example a Http
 * Session.
 * </p>
 *
 * @author Pete Muir
 * @see BoundContext
 */
public interface BoundBeanStore extends BeanStore {

    /**
     * Detach the context
     *
     * @return true if the bean store was detached, or false if the bean store is
     *         already detached
     */
    boolean detach();

    /**
     * Attach the context
     *
     * @return true if the bean store was attached, or false if the bean store is
     *         already attached
     */
    boolean attach();

    /**
     * Return true if the bean store is attached
     *
     * @return true if the bean store is attached or false if the bean store has
     *         been detached
     */
    boolean isAttached();

}
