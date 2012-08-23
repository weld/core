/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.se;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.api.helpers.ForwardingBootstrap;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.environment.se.discovery.url.WeldSEResourceLoader;
import org.jboss.weld.environment.se.discovery.url.WeldSEUrlDeployment;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.jboss.weld.metadata.MetadataImpl;
import org.jboss.weld.resources.spi.ResourceLoader;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * The preferred method of booting Weld SE.
 * </p>
 * <p/>
 * <p>
 * Typical usage of this API looks like this:
 * </p>
 * <p/>
 * <pre>
 * WeldContainer weld = new Weld().initialize();
 * weld.instance().select(Foo.class).get();
 * weld.event().select(Bar.class).fire(new Bar());
 * weld.shutdown();
 * </pre>
 *
 * @author Peter Royle
 * @author Pete Muir
 * @author Ales Justin
 */
public class Weld {

    private static final String BOOTSTRAP_IMPL_CLASS_NAME = "org.jboss.weld.bootstrap.WeldBootstrap";

    private ShutdownManager shutdownManager;
    private Set<Metadata<Extension>> extensions;

    /**
     * Add extension explicitly.
     *
     * @param extension an extension
     */
    public void addExtension(Extension extension) {
        if (extensions == null)
            extensions = new HashSet<Metadata<Extension>>();
        extensions.add(new MetadataImpl<Extension>(extension, "<explicity-added>"));
    }

    /**
     * Boots Weld and creates and returns a WeldContainer instance, through which
     * beans and events can be accessed.
     *
     * @return weld container
     */
    public WeldContainer initialize() {
        ResourceLoader resourceLoader = new WeldSEResourceLoader();
        // check for beans.xml
        if (resourceLoader.getResource(WeldSEUrlDeployment.BEANS_XML) == null)
            throw new IllegalStateException("Missing beans.xml file in META-INF!");

        final Bootstrap delegate;
        try {
            delegate = (Bootstrap) resourceLoader.classForName(BOOTSTRAP_IMPL_CLASS_NAME).newInstance();
        } catch (InstantiationException ex) {
            throw new IllegalStateException("Error loading Weld bootstrap, check that Weld is on the classpath", ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Error loading Weld bootstrap, check that Weld is on the classpath", ex);
        }

        Bootstrap bootstrap = new ForwardingBootstrap() {
            protected Bootstrap delegate() {
                return delegate;
            }

            public BeansXml parse(URL url) {
                return delegate.parse(url);
            }

            public BeansXml parse(Iterable<URL> urls) {
                return delegate.parse(urls);
            }

            public BeansXml parse(Iterable<URL> urls, boolean removeDuplicates) {
                return delegate.parse(urls, removeDuplicates);
            }

            public Iterable<Metadata<Extension>> loadExtensions(ClassLoader classLoader) {
                Iterable<Metadata<Extension>> iter = delegate.loadExtensions(classLoader);
                if (extensions != null) {
                    Set<Metadata<Extension>> set = new HashSet<Metadata<Extension>>(extensions);
                    for (Metadata<Extension> ext : iter)
                        set.add(ext);
                    return set;
                } else {
                    return iter;
                }
            }
        };

        Deployment deployment = createDeployment(resourceLoader, bootstrap);
        // Set up the container
        bootstrap.startContainer(Environments.SE, deployment);

        // Start the container
        bootstrap.startInitialization();
        bootstrap.deployBeans();
        bootstrap.validateBeans();
        bootstrap.endInitialization();

        // Set up the ShutdownManager for later
        this.shutdownManager = getInstanceByType(bootstrap.getManager(deployment.loadBeanDeploymentArchive(ShutdownManager.class)), ShutdownManager.class);
        this.shutdownManager.setBootstrap(bootstrap);

        WeldContainer container = getInstanceByType(bootstrap.getManager(deployment.loadBeanDeploymentArchive(WeldContainer.class)), WeldContainer.class);

        // notify container initialized
        container.event().select(ContainerInitialized.class).fire(new ContainerInitialized());

        return container;
    }

    /**
     * <p>
     * Extensions to Weld SE can subclass and override this method to customise
     * the deployment before weld boots up. For example, to add a custom
     * ResourceLoader, you would subclass Weld like so:
     * </p>
     * <p/>
     * <pre>
     * public class MyWeld extends Weld
     * {
     *    protected Deployment createDeployment()
     *    {
     *       Deployment deployment = super.createDeployment();
     *       deployment.getServices().add(ResourceLoader.class, new MyResourceLoader());
     *       return deployment;
     *    }
     * }
     * </pre>
     * <p/>
     * <p>
     * This could then be used as normal:
     * </p>
     * <p/>
     * <pre>
     * WeldContainer container = new MyWeld().initialize();
     * </pre>
     */
    protected Deployment createDeployment(ResourceLoader resourceLoader, Bootstrap bootstrap) {
        return new WeldSEUrlDeployment(resourceLoader, bootstrap);
    }

    /**
     * Utility method allowing managed instances of beans to provide entry points
     * for non-managed beans (such as {@link WeldContainer}). Should only called
     * once Weld has finished booting.
     *
     * @param manager  the BeanManager to use to access the managed instance
     * @param type     the type of the Bean
     * @param bindings the bean's qualifiers
     * @return a managed instance of the bean
     * @throws IllegalArgumentException       if the given type represents a type
     *                                        variable
     * @throws IllegalArgumentException       if two instances of the same qualifier
     *                                        type are given
     * @throws IllegalArgumentException       if an instance of an annotation that is
     *                                        not a qualifier type is given
     * @throws UnsatisfiedResolutionException if no beans can be resolved * @throws
     *                                        AmbiguousResolutionException if the ambiguous dependency
     *                                        resolution rules fail
     * @throws IllegalArgumentException       if the given type is not a bean type of
     *                                        the given bean
     */
    protected <T> T getInstanceByType(BeanManager manager, Class<T> type, Annotation... bindings) {
        final Bean<?> bean = manager.resolve(manager.getBeans(type, bindings));
        if (bean == null) {
            throw new UnsatisfiedResolutionException("Unable to resolve a bean for " + type + " with bindings " + Arrays.asList(bindings));
        }
        CreationalContext<?> cc = manager.createCreationalContext(bean);
        return type.cast(manager.getReference(bean, type, cc));
    }

    /**
     * Shuts down Weld.
     */
    public void shutdown() {
        shutdownManager.shutdown();
    }
}
