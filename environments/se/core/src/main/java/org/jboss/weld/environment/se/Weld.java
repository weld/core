/*
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

import java.lang.annotation.Annotation;
import java.net.URL;
import java.security.AccessController;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.api.TypeDiscoveryConfiguration;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.environment.se.discovery.WeldSEBeanDeploymentArchive;
import org.jboss.weld.environment.se.discovery.WeldSEClassFileServices;
import org.jboss.weld.environment.se.discovery.url.DefaultDiscoveryStrategy;
import org.jboss.weld.environment.se.discovery.url.DiscoveryStrategy;
import org.jboss.weld.environment.se.discovery.url.WeldSEResourceLoader;
import org.jboss.weld.environment.se.discovery.url.WeldSEUrlDeployment;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.jboss.weld.literal.InitializedLiteral;
import org.jboss.weld.metadata.MetadataImpl;
import org.jboss.weld.resources.spi.ClassFileServices;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.security.GetSystemPropertyAction;
import org.jboss.weld.util.reflection.Reflections;

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
 * Weld weld = new Weld();
 * WeldContainer container = weld.initialize();
 * container.instance().select(Foo.class).get();
 * container.event().select(Bar.class).fire(new Bar());
 * weld.shutdown();
 * </pre>
 *
 * @author Peter Royle
 * @author Pete Muir
 * @author Ales Justin
 */
public class Weld {

    private static final String JANDEX_ENABLED_DISCOVERY_STRATEGY_CLASS_NAME = "org.jboss.weld.environment.se.discovery.url.JandexEnabledDiscoveryStrategy";
    public static final String COMPOSITE_ARCHIVE_ENABLEMENT_SYSTEM_PROPERTY = "org.jboss.weld.se.archive.isolation";
    private static final String BOOTSTRAP_IMPL_CLASS_NAME = "org.jboss.weld.bootstrap.WeldBootstrap";
    private static final String ERROR_LOADING_WELD_BOOTSTRAP_EXC_MESSAGE = "Error loading Weld bootstrap, check that Weld is on the classpath";
    public static final String JANDEX_INDEX_CLASS_NAME = "org.jboss.jandex.Index";
    private static final String JANDEX_ENABLED_DISCOVERY_STRATEGY_CLASS_STRING = JANDEX_ENABLED_DISCOVERY_STRATEGY_CLASS_NAME;


    private ShutdownManager shutdownManager;
    private Set<Metadata<Extension>> extensions;
    private DiscoveryStrategy strategy;

    /**
     * Add extension explicitly.
     *
     * @param extension an extension
     */
    public void addExtension(Extension extension) {
        if (extensions == null) {
            extensions = new HashSet<Metadata<Extension>>();
        }
        extensions.add(new MetadataImpl<Extension>(extension, "<explicitly-added>"));
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
        if (resourceLoader.getResource(WeldSEUrlDeployment.BEANS_XML) == null) {
            throw new IllegalStateException("Missing beans.xml file in META-INF!");
        }

        final CDI11Bootstrap bootstrap;
        try {
            bootstrap = (CDI11Bootstrap) resourceLoader.classForName(BOOTSTRAP_IMPL_CLASS_NAME).newInstance();
        } catch (InstantiationException ex) {
            throw new IllegalStateException(ERROR_LOADING_WELD_BOOTSTRAP_EXC_MESSAGE, ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ERROR_LOADING_WELD_BOOTSTRAP_EXC_MESSAGE, ex);
        }

        Deployment deployment = createDeployment(resourceLoader, bootstrap);


        // Set up the container
        bootstrap.startContainer(Environments.SE, deployment);
        // Start the container
        bootstrap.startInitialization();
        bootstrap.deployBeans();
        bootstrap.validateBeans();
        bootstrap.endInitialization();

        final BeanManager manager = bootstrap.getManager(deployment.loadBeanDeploymentArchive(WeldContainer.class));

        // Set up the ShutdownManager for later
        this.shutdownManager = new ShutdownManager(bootstrap, manager);

        WeldContainer container = getInstanceByType(manager, WeldContainer.class);

        // notify container initialized
        container.event().select(ContainerInitialized.class, InitializedLiteral.APPLICATION).fire(new ContainerInitialized());

        return container;
    }

    private Iterable<Metadata<Extension>> loadExtensions(ClassLoader classLoader, Bootstrap bootstrap) {
        Iterable<Metadata<Extension>> iter = bootstrap.loadExtensions(classLoader);
        if (extensions != null) {
            Set<Metadata<Extension>> set = new HashSet<Metadata<Extension>>(extensions);
            for (Metadata<Extension> ext : iter) {
                set.add(ext);
            }
            return set;
        } else {
            return iter;
        }
    }

    /**
     * <p>
     * Extensions to Weld SE can subclass and override this method to customize the deployment before weld boots up. For example, to add a custom
     * ResourceLoader, you would subclass Weld like so:
     * </p>
     * <p/>
     *
     * <pre>
     * public class MyWeld extends Weld {
     *     protected Deployment createDeployment(ResourceLoader resourceLoader, CDI11Bootstrap bootstrap) {
     *         return super.createDeployment(new MyResourceLoader(), bootstrap);
     *     }
     * }
     * </pre>
     * <p/>
     * <p>
     * This could then be used as normal:
     * </p>
     * <p/>
     *
     * <pre>
     * WeldContainer container = new MyWeld().initialize();
     * </pre>
     *
     * @param resourceLoader
     * @param bootstrap
     * @param strategy strategy of discovering the bean archives
     */
    protected Deployment createDeployment(ResourceLoader resourceLoader, CDI11Bootstrap bootstrap) {
        Iterable<Metadata<Extension>> loadedExtensions = loadExtensions(WeldSEResourceLoader.getClassLoader(), bootstrap);
        TypeDiscoveryConfiguration typeDiscoveryConfiguration = bootstrap.startExtensions(loadedExtensions);
        if (Reflections.isClassLoadable(JANDEX_INDEX_CLASS_NAME, resourceLoader)) {
            Class<?> clazz = Reflections.loadClass(JANDEX_ENABLED_DISCOVERY_STRATEGY_CLASS_STRING, resourceLoader);
            try {
                strategy = (DiscoveryStrategy) clazz.getConstructor(ResourceLoader.class, Bootstrap.class, TypeDiscoveryConfiguration.class).newInstance(resourceLoader, bootstrap, typeDiscoveryConfiguration);
            } catch (Exception e) {
                throw new IllegalStateException("Unable to instantiate jandex discovery strategy", e);
            }
        } else {
            strategy = new DefaultDiscoveryStrategy(resourceLoader, bootstrap);
        }
        Set<WeldSEBeanDeploymentArchive> discoveredArchives = strategy.discoverArchives();

        String isolation = AccessController.doPrivileged(new GetSystemPropertyAction(COMPOSITE_ARCHIVE_ENABLEMENT_SYSTEM_PROPERTY));
        Deployment deployment=null;
        if (isolation != null && Boolean.valueOf(isolation).equals(Boolean.FALSE)) {
            WeldSEBeanDeploymentArchive archive = mergeToOne(bootstrap, discoveredArchives);
            deployment = new WeldSEUrlDeployment(resourceLoader, bootstrap, Collections.singleton(archive), loadedExtensions);
        } else {
            deployment=  new WeldSEUrlDeployment(resourceLoader, bootstrap, discoveredArchives, loadedExtensions);
        }

        if (strategy.getClass().getName().equals(JANDEX_ENABLED_DISCOVERY_STRATEGY_CLASS_NAME)) {
            ClassFileServices classFileServices = new WeldSEClassFileServices(strategy);
            deployment.getServices().add(ClassFileServices.class, classFileServices);
        }
        return deployment;
    }

    /**
     * Method merging more BeanDeploymentArchives to one. This covers merging all the beans.xml and all the found classes.
     */
    private WeldSEBeanDeploymentArchive mergeToOne(CDI11Bootstrap bootstrap, Collection<WeldSEBeanDeploymentArchive> discoveredArchives) {
        Set<String> beanClasses = new HashSet<String>();
        Set<URL> urls = new HashSet<URL>();
        for (BeanDeploymentArchive archive : discoveredArchives) {
            beanClasses.addAll(archive.getBeanClasses());
            urls.add(archive.getBeansXml().getUrl());
        }
        BeansXml beansXml = bootstrap.parse(urls, true);
        WeldSEBeanDeploymentArchive archive = new WeldSEBeanDeploymentArchive("main", beanClasses, beansXml);
        return archive;
    }

    /**
     * Utility method allowing managed instances of beans to provide entry points for non-managed beans (such as {@link WeldContainer}). Should only called once
     * Weld has finished booting.
     *
     * @param manager the BeanManager to use to access the managed instance
     * @param type the type of the Bean
     * @param bindings the bean's qualifiers
     * @return a managed instance of the bean
     * @throws IllegalArgumentException if the given type represents a type variable
     * @throws IllegalArgumentException if two instances of the same qualifier type are given
     * @throws IllegalArgumentException if an instance of an annotation that is not a qualifier type is given
     * @throws UnsatisfiedResolutionException if no beans can be resolved * @throws AmbiguousResolutionException if the ambiguous dependency resolution rules
     *         fail
     * @throws IllegalArgumentException if the given type is not a bean type of the given bean
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
        if (shutdownManager != null) {
            shutdownManager.shutdown();
        }
    }
}
