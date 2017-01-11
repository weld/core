package org.jboss.weld.contexts.beanstore;

import java.util.Collection;
import java.util.Iterator;

import org.jboss.weld.serialization.spi.BeanIdentifier;

public interface NamingScheme {

    /**
     * Determine if this identifier has been prefixed
     *
     * @param id the id to check
     * @return true if it has been prefixed, false otherwise
     */
    boolean accept(String id);

    /**
     * Remove the prefix from the id
     *
     * @param id the prefixed id
     * @return the id without the prefix
     */
    BeanIdentifier deprefix(String id);

    /**
     * Add the prefix to the id
     *
     * @param id the id to prefix
     * @return the prefixed id
     */
    String prefix(BeanIdentifier id);

    /**
     * Filter ids and retain only those correctly prefixed.
     *
     * @param ids the identifiers to filter
     */
    Collection<String> filterIds(Iterator<String> ids);

    Collection<BeanIdentifier> deprefix(Collection<String> ids);

    Collection<String> prefix(Collection<BeanIdentifier> ids);

}
