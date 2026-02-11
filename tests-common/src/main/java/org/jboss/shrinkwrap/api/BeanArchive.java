package org.jboss.shrinkwrap.api;

import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.BeansXml;

/**
 * A CDI specific extension to Shrinkwrap to ease Weld testing.
 */
public interface BeanArchive extends JavaArchive {

    /**
     * Adds Decorators to the beans.xml.
     *
     * @param classes
     * @return
     */
    BeanArchive decorate(Class<?>... classes);

    /**
     * Adds Interceptors to the beans.xml.
     *
     * @param classes
     * @return
     */
    BeanArchive intercept(Class<?>... classes);

    /**
     * Adds Alternatives to the beans.xml.
     *
     * @param classes
     * @return
     */
    BeanArchive alternate(Class<?>... classes);

    /**
     * Adds a Stereotype Alternative to beans.xml.
     *
     * @param classes
     * @return
     */
    BeanArchive stereotype(Class<?>... classes);

    /**
     * Adds an exclude filter to beans.xml.
     *
     * @param excludes
     * @return
     */
    BeanArchive exclude(BeansXml.Exclude... excludes);

    BeanArchive beanDiscoveryMode(BeanDiscoveryMode mode);

    /**
     * Sets bean-discovery-mode to annotated
     */
    BeanArchive annotated();

    /**
     * Adds the trim element to beans.xml
     */
    BeanArchive trim();
}
