package org.jboss.weld.environment.osgi.api.utils;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ClassLoaderLoader implements Loader {
    private final ClassLoader cl;

    public ClassLoaderLoader(ClassLoader cl) {
        this.cl = cl;
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return cl.loadClass(name);
    }
}
