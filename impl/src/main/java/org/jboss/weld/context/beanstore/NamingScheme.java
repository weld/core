package org.jboss.weld.context.beanstore;

import java.util.Collection;

public interface NamingScheme {

    /**
     * Determine if this identifier has been prefixed
     *
     * @param id the id to check
     * @return true if it has been prefixed, false otherwise
     */
    public abstract boolean accept(String id);

    /**
     * Remove the prefix from the id
     *
     * @param id the prefixed id
     * @return the id without the prefix
     */
    public abstract String deprefix(String id);

    /**
     * Add the prefix to the id
     *
     * @param id the id to prefix
     * @return the prefixed id
     */
    public abstract String prefix(String id);

    /**
     * Filter a collection of ids, retaining only those correctly prefixed.
     *
     * @param ids the collection of ides to filter
     */
    public abstract Collection<String> filterIds(Collection<String> ids);

    public abstract Collection<String> deprefix(Collection<String> ids);

    public abstract Collection<String> prefix(Collection<String> ids);

}