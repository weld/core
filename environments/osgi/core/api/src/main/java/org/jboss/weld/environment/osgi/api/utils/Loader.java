package org.jboss.weld.environment.osgi.api.utils;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface Loader {
    Class<?> loadClass(String name) throws ClassNotFoundException;
}
