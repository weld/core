package org.jboss.weld.environment.osgi.api.utils;

import org.osgi.framework.Bundle;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class BundleLoader implements Loader {
    private final Bundle bundle;

    public BundleLoader(Bundle bundle) {
        this.bundle = bundle;
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return bundle.loadClass(name);
    }
}
