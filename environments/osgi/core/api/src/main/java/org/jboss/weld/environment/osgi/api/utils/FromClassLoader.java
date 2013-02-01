package org.jboss.weld.environment.osgi.api.utils;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class FromClassLoader extends ClassLoaderLoader {
    public FromClassLoader(Class<?> clazz) {
        super(getClassLoader(clazz));
    }

    private static ClassLoader getClassLoader(final Class<?> clazz) {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return clazz.getClassLoader();
        } else {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                public java.lang.ClassLoader run() {
                    return clazz.getClassLoader();
                }
            });
        }
    }
}
