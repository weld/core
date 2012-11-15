package org.jboss.shrinkwrap.api;

import org.jboss.shrinkwrap.api.spec.JavaArchive;

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

    BeanArchive decorate(BeansXmlClass... classes);

    /**
     * Adds Interceptors to the beans.xml.
     *
     * @param classes
     * @return
     */
    BeanArchive intercept(Class<?>... classes);

    BeanArchive intercept(BeansXmlClass... classes);

    /**
     * Adds Alternatives to the beans.xml.
     *
     * @param classes
     * @return
     */
    BeanArchive alternate(Class<?>... classes);

    BeanArchive alternate(BeansXmlClass... classes);

    /**
     * Adds a Stereotype Alternative to beans.xml.
     *
     * @param classes
     * @return
     */
    BeanArchive stereotype(Class<?>... classes);
}
