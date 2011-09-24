package org.jboss.weld.resources;

import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.collections.EnumerationList;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

/**
 * A (@link ResourceLoader} implementation that uses a specific @{link ClassLoader}
 *
 * @author Marius Bogoevici
 */
public class ClassLoaderResourceLoader implements ResourceLoader {
    private ClassLoader classLoader;

    public ClassLoaderResourceLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Class<?> classForName(String name) {
        try {
            return classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new ResourceLoadingException("Error loading class " + name, e);
        } catch (NoClassDefFoundError e) {
            throw new ResourceLoadingException("Error loading class " + name, e);
        } catch (TypeNotPresentException e) {
            throw new ResourceLoadingException("Error loading class " + name, e);
        }
    }

    public URL getResource(String name) {
        return classLoader.getResource(name);

    }

    public Collection<URL> getResources(String name) {
        try {
            return new EnumerationList<URL>(classLoader.getResources(name));
        } catch (IOException e) {
            throw new ResourceLoadingException("Error loading resource " + name, e);
        }
    }

    public void cleanup() {
        this.classLoader = null;
    }
}
