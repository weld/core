package org.jboss.weld.tests.unit.proxy;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.jboss.weld.bean.proxy.ClientProxyProvider;
import org.jboss.weld.resources.DefaultResourceLoader;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ClientProxyProviderTest {

    @Test
    public void testNewInstance() throws MalformedURLException {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        assertNotNull(contextClassLoader);
        final URL myLocation = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        assertNotNull(myLocation);
        final URLClassLoader classLoader =
            new URLClassLoader(new URL[] { new URL(myLocation, this.getClass().getSimpleName() + "/") }, contextClassLoader);
        final ClientProxyProvider clientProxyProvider = ClientProxyProvider.newInstance(new DefaultResourceLoader() {
                @Override
                protected final ClassLoader classLoader() {
                    return classLoader;
                }
            }, "bogusContainerId");
        assertTrue(clientProxyProvider instanceof FancyClientProxyProvider);
    }

    private static final class FancyClientProxyProvider extends ClientProxyProvider {

        private FancyClientProxyProvider() {
            super();
        }

    }

}
