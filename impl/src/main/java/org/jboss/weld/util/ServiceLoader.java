/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.metadata.FileMetadata;
import org.jboss.weld.resources.ClassLoaderResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This class handles looking up service providers on the class path. It
 * implements the <a href="http://java.sun.com/javase/6/docs/technotes/guides/jar/jar.html#Service%20Provider"
 * >Service Provider section of the JAR File Specification</a>.
 * <p/>
 * The Service Provider programmatic lookup was not specified prior to Java 6 so
 * this interface allows use of the specification prior to Java 6.
 * <p/>
 * The API is copied from <a
 * href="http://java.sun.com/javase/6/docs/api/java/util/ServiceLoader.html"
 * >java.util.ServiceLoader</a> and enhanced to support the {@link Metadata}
 * contract.
 *
 * @author Pete Muir
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @author Nicklas Karlsson
 */
public class ServiceLoader<S> implements Iterable<Metadata<S>> {

    private static final String ERROR_INSTANTIATING = "Error instantiating ";

    private static final String SERVICES = "META-INF/services";

    private static final Logger log = Logger.getLogger("ServiceLoader");

    /**
     * Creates a new service loader for the given service type, using the current
     * thread's context class loader.
     * <p/>
     * An invocation of this convenience method of the form
     * <p/>
     * {@code ServiceLoader.load(service)</code>}
     * <p/>
     * is equivalent to
     * <p/>
     * <code>ServiceLoader.load(service,
     * Thread.currentThread().getContextClassLoader())</code>
     *
     * @param service The interface or abstract class representing the service
     * @return A new service loader
     */
    public static <S> ServiceLoader<S> load(Class<S> service) {
        return load(service, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Creates a new service loader for the given service type and class loader.
     *
     * @param service The interface or abstract class representing the service
     * @param loader The class loader to be used to load provider-configuration
     *        files and provider classes, or null if the system class loader
     *        (or, failing that, the bootstrap class loader) is to be used
     * @return A new service loader
     */
    public static <S> ServiceLoader<S> load(Class<S> service, ClassLoader loader) {
        if (loader == null) {
            loader = service.getClassLoader();
        }
        return new ServiceLoader<S>(service, new ClassLoaderResourceLoader(loader));
    }

    public static <S> ServiceLoader<S> load(Class<S> service, ResourceLoader loader) {
        if (loader == null) {
            return load(service, service.getClassLoader());
        }
        return new ServiceLoader<S>(service, loader);
    }

    /**
     * Creates a new service loader for the given service type, using the
     * extension class loader.
     * <p/>
     * This convenience method simply locates the extension class loader, call it
     * extClassLoader, and then returns
     * <p/>
     * <code>ServiceLoader.load(service, extClassLoader)</code>
     * <p/>
     * If the extension class loader cannot be found then the system class loader
     * is used; if there is no system class loader then the bootstrap class
     * loader is used.
     * <p/>
     * This method is intended for use when only installed providers are desired.
     * The resulting service will only find and load providers that have been
     * installed into the current Java virtual machine; providers on the
     * application's class path will be ignored.
     *
     * @param service The interface or abstract class representing the service
     * @return A new service loader
     */
    public static <S> ServiceLoader<S> loadInstalled(Class<S> service) {
        throw new UnsupportedOperationException("Not implemented");
    }

    private final String serviceFile;
    private Class<S> expectedType;
    private final ResourceLoader loader;

    private Set<Metadata<S>> providers;

    private ServiceLoader(Class<S> service, ResourceLoader loader) {
        this.loader = loader;
        this.serviceFile = SERVICES + "/" + service.getName();
        this.expectedType = service;
    }

    /**
     * Clear this loader's provider cache so that all providers will be reloaded.
     * <p/>
     * After invoking this method, subsequent invocations of the iterator method
     * will lazily look up and instantiate providers from scratch, just as is
     * done by a newly-created loader.
     * <p/>
     * This method is intended for use in situations in which new providers can
     * be installed into a running Java virtual machine.
     */
    public void reload() {
        providers = new HashSet<Metadata<S>>();

        for (URL serviceFile : loadServiceFiles()) {
            loadServiceFile(serviceFile);
        }
    }

    private List<URL> loadServiceFiles() {
        return new ArrayList<URL>(loader.getResources(serviceFile));
    }

    @SuppressFBWarnings(value = "OS_OPEN_STREAM", justification = "False positive")
    private void loadServiceFile(URL serviceFile) {
        InputStream is = null;
        try {
            URLConnection jarConnection = serviceFile.openConnection();
            //Don't cache the file (avoids file leaks on GlassFish).
            jarConnection.setUseCaches(false);
            is = jarConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String serviceClassName = null;
            int i = 0;
            while ((serviceClassName = reader.readLine()) != null) {
                i++;
                serviceClassName = trim(serviceClassName);
                if (serviceClassName.length() > 0) {
                    loadService(serviceClassName, serviceFile, i);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read services file " + serviceFile, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new RuntimeException("Could not close services file " + serviceFile, e);
                }
            }
        }
    }

    private String trim(String line) {
        final int comment = line.indexOf('#');

        if (comment > -1) {
            line = line.substring(0, comment);
        }
        return line.trim();
    }

    private void loadService(String serviceClassName, URL file, int lineNumber) {
        Class<? extends S> serviceClass = loadClass(serviceClassName);
        if (serviceClass == null) {
            return;
        }
        S serviceInstance = prepareInstance(serviceClass);
        if (serviceInstance == null) {
            return;
        }
        providers.add(new FileMetadata<S>(serviceInstance, file, lineNumber));
    }

    private Class<? extends S> loadClass(String serviceClassName) {
        Class<?> clazz = null;
        Class<? extends S> serviceClass = null;
        try {
            clazz = loader.classForName(serviceClassName);
            serviceClass = clazz.asSubclass(expectedType);
        } catch (ResourceLoadingException e) {
            log.warning("Could not load service class " + serviceClassName);
        } catch (ClassCastException e) {
            throw new RuntimeException("Service class " + serviceClassName + " didn't implement the required interface");
        }
        return serviceClass;
    }

    private S prepareInstance(final Class<? extends S> serviceClass) {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            return AccessController.doPrivileged(new PrivilegedAction<S>() {

                @Override
                public S run() {
                    return createInstance(serviceClass);
                }
            });
        } else {
            return createInstance(serviceClass);
        }
    }

    /**
     * Lazily loads the available providers of this loader's service.
     * <p/>
     * The iterator returned by this method first yields all of the elements of
     * the provider cache, in instantiation order. It then lazily loads and
     * instantiates any remaining providers, adding each one to the cache in
     * turn.
     * <p/>
     * To achieve laziness the actual work of parsing the available
     * provider-configuration files and instantiating providers must be done by
     * the iterator itself. Its hasNext and next methods can therefore throw a
     * ServiceConfigurationError if a provider-configuration file violates the
     * specified format, or if it names a provider class that cannot be found and
     * instantiated, or if the result of instantiating the class is not
     * assignable to the service type, or if any other kind of exception or error
     * is thrown as the next provider is located and instantiated. To write
     * robust code it is only necessary to catch ServiceConfigurationError when
     * using a service iterator.
     * <p/>
     * If such an error is thrown then subsequent invocations of the iterator
     * will make a best effort to locate and instantiate the next available
     * provider, but in general such recovery cannot be guaranteed.
     * <p/>
     * Design Note Throwing an error in these cases may seem extreme. The
     * rationale for this behavior is that a malformed provider-configuration
     * file, like a malformed class file, indicates a serious problem with the
     * way the Java virtual machine is configured or is being used. As such it is
     * preferable to throw an error rather than try to recover or, even worse,
     * fail silently.
     * <p/>
     * The iterator returned by this method does not support removal. Invoking
     * its remove method will cause an UnsupportedOperationException to be
     * thrown.
     *
     * @return An iterator that lazily loads providers for this loader's service
     */
    public Iterator<Metadata<S>> iterator() {
        if (providers == null) {
            reload();
        }
        return providers.iterator();
    }

    /**
     * Returns a string describing this service.
     *
     * @return A descriptive string
     */
    @Override
    public String toString() {
        return "Services for " + serviceFile;
    }

    public Stream<Metadata<S>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    private <S> S createInstance(Class<? extends S> serviceClass) {
        Constructor<? extends S> constructor = null;
        try {
            constructor = serviceClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Throwable t) {
            throw new ServiceConfigurationError(ERROR_INSTANTIATING + ":" + serviceClass.getName(), t);
        }
    }
}
