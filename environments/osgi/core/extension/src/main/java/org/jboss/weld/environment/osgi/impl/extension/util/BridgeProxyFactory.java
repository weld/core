package org.jboss.weld.environment.osgi.impl.extension.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Hack around ProxyFactory visibility issues.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class BridgeProxyFactory extends ProxyFactory {
    private static final Map<Bundle, ClassLoader> map = new HashMap<Bundle, ClassLoader>();

    public static void clear(Bundle bundle) {
        synchronized (map) {
            map.remove(bundle);
        }
    }

    public BridgeProxyFactory(Class<?> superClass) {
        setSuperclass(superClass); // expose impl
        setFilter(FINALIZE_FILTER);
    }

    protected ClassLoader getClassLoader() {
        Bundle bundle = FrameworkUtil.getBundle(getSuperclass());
        synchronized (map) {
            ClassLoader cl = map.get(bundle);
            if (cl == null) {
                cl = new BridgeClassLoader(super.getClassLoader());
            }
            return cl;
        }
    }

    private static class BridgeClassLoader extends ClassLoader {
        private BridgeClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (name.startsWith("javassist.")) {
                return getClass().getClassLoader().loadClass(name);
            } else {
                return super.loadClass(name);
            }
        }
    }

    private static final MethodFilter FINALIZE_FILTER = new MethodFilter() {
        public boolean isHandled(Method m) {
            // skip finalize methods
            return !("finalize".equals(m.getName()) && m.getParameterTypes().length == 0);
        }
    };
}
