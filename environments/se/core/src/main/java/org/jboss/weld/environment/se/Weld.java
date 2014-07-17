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

import org.jboss.logging.Logger;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.api.SingletonProvider;
import org.jboss.weld.bootstrap.api.TypeDiscoveryConfiguration;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.environment.se.discovery.WeldSEBeanDeploymentArchive;
import org.jboss.weld.environment.se.discovery.url.DefaultDiscoveryStrategy;
import org.jboss.weld.environment.se.discovery.url.DiscoveryStrategy;
import org.jboss.weld.environment.se.discovery.url.WeldSEResourceLoader;
import org.jboss.weld.environment.se.discovery.url.WeldSEUrlDeployment;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.jboss.weld.environment.se.logging.WeldSELogger;
import org.jboss.weld.environment.se.util.SEReflections;
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

    private static final String SYSTEM_PROPERTY_STRING = "System property ";
    private static final Logger log = Logger.getLogger(Weld.class);
    private static final String JANDEX_ENABLED_DISCOVERY_STRATEGY_CLASS_NAME = "org.jboss.weld.environment.se.discovery.url.JandexEnabledDiscoveryStrategy";
    private static final String CLASS_FILE_SERVICES_CLASS_NAME = "org.jboss.weld.environment.se.discovery.WeldSEClassFileServices";
    public static final String ARCHIVE_ISOLATION_SYSTEM_PROPERTY = "org.jboss.weld.se.archive.isolation";
    private static final String BOOTSTRAP_IMPL_CLASS_NAME = "org.jboss.weld.bootstrap.WeldBootstrap";
    public static final String JANDEX_INDEX_CLASS_NAME = "org.jboss.jandex.Index";

    static {
        if (!(SingletonProvider.instance() instanceof RegistrySingletonProvider)) {
            // make sure RegistrySingletonProvider is used (required for supporting multiple parallel Weld instances)
            SingletonProvider.reset();
            SingletonProvider.initialize(new RegistrySingletonProvider());
        }
    }

    private ShutdownManager shutdownManager;
    private Set<Metadata<Extension>> extensions;

    private final String containerId;

    public Weld() {
        this(RegistrySingletonProvider.STATIC_INSTANCE);
    }

    /**
     * Assign a unique ID to this Weld instance. This is required when running multiple Weld containers
     * at once.
     * @param containerId the unique ID for this container
     */
    public Weld(String containerId) {
        this.containerId = containerId;
    }

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
            throw WeldSELogger.LOG.missingBeansXml();
        }

        final CDI11Bootstrap bootstrap;
        try {
            bootstrap = (CDI11Bootstrap) resourceLoader.classForName(BOOTSTRAP_IMPL_CLASS_NAME).newInstance();
        } catch (InstantiationException ex) {
            throw WeldSELogger.LOG.errorLoadingWeld();
        } catch (IllegalAccessException ex) {
            throw WeldSELogger.LOG.errorLoadingWeld();
        }

        Deployment deployment = createDeployment(resourceLoader, bootstrap);


        // Set up the container
        bootstrap.startContainer(containerId, Environments.SE, deployment);
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
        final Iterable<Metadata<Extension>> loadedExtensions = loadExtensions(WeldSEResourceLoader.getClassLoader(), bootstrap);
        final TypeDiscoveryConfiguration typeDiscoveryConfiguration = bootstrap.startExtensions(loadedExtensions);
        DiscoveryStrategy strategy;
        if (Reflections.isClassLoadable(JANDEX_INDEX_CLASS_NAME, resourceLoader)) {
            strategy = SEReflections.newInstance(resourceLoader, JANDEX_ENABLED_DISCOVERY_STRATEGY_CLASS_NAME, resourceLoader, bootstrap, typeDiscoveryConfiguration);
        } else {
            strategy = new DefaultDiscoveryStrategy(resourceLoader, bootstrap);
            log.debug("For the deployment, DefaultDiscoveryStrategy is used.");
        }
        Set<WeldSEBeanDeploymentArchive> discoveredArchives = strategy.discoverArchives();

        String isolation = AccessController.doPrivileged(new GetSystemPropertyAction(ARCHIVE_ISOLATION_SYSTEM_PROPERTY));
        Deployment deployment=null;
        if (isolation != null && Boolean.valueOf(isolation).equals(Boolean.FALSE)) {
            log.debug(SYSTEM_PROPERTY_STRING + ARCHIVE_ISOLATION_SYSTEM_PROPERTY
                    + " is set to false value, so only one bean archive will be created.");
            WeldSEBeanDeploymentArchive archive = mergeToOne(bootstrap, discoveredArchives);
            deployment = new WeldSEUrlDeployment(resourceLoader, bootstrap, Collections.singleton(archive), loadedExtensions);
        } else {
            log.debug(SYSTEM_PROPERTY_STRING + ARCHIVE_ISOLATION_SYSTEM_PROPERTY
                    + " is on default true value, creating multiple bean archives if needed.");
            deployment=  new WeldSEUrlDeployment(resourceLoader, bootstrap, discoveredArchives, loadedExtensions);
        }

        if (strategy.getClass().getName().equals(JANDEX_ENABLED_DISCOVERY_STRATEGY_CLASS_NAME)) {
            final ClassFileServices classFileServices = SEReflections.<ClassFileServices>newInstance(resourceLoader, CLASS_FILE_SERVICES_CLASS_NAME, strategy);
            deployment.getServices().add(ClassFileServices.class, classFileServices);
        }
        return deployment;
    }

    /**
     * Method merging more BeanDeploymentArchives to one. This covers merging all the beans.xml into one and making a collection of all the found classes.
     */
    private WeldSEBeanDeploymentArchive mergeToOne(CDI11Bootstrap bootstrap, Collection<WeldSEBeanDeploymentArchive> discoveredArchives) {
        Set<String> beanClasses = new HashSet<String>();
        Set<URL> urls = new HashSet<URL>();
        for (BeanDeploymentArchive archive : discoveredArchives) {
            beanClasses.addAll(archive.getBeanClasses());
            if (archive.getBeansXml() != BeansXml.EMPTY_BEANS_XML) {
                urls.add(archive.getBeansXml().getUrl());
            }
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
            throw WeldSELogger.LOG.unableToResolveBean(type, Arrays.asList(bindings));
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
