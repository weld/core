package org.jboss.weld.resources;

/**
 * A (@link ResourceLoader} implementation that uses a specific @{link ClassLoader}
 *
 * @author Marius Bogoevici
 *
 */
public class ClassLoaderResourceLoader extends AbstractClassLoaderResourceLoader {
    private ClassLoader classLoader;

    public ClassLoaderResourceLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    protected ClassLoader classLoader() {
        return classLoader;
    }

    public void cleanup() {
        this.classLoader = null;
    }
}
