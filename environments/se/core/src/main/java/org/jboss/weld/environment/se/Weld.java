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

import static org.jboss.weld.environment.util.URLUtils.JAR_URL_SEPARATOR;
import static org.jboss.weld.environment.util.URLUtils.PROCOTOL_FILE;
import static org.jboss.weld.environment.util.URLUtils.PROCOTOL_JAR;
import static org.jboss.weld.environment.util.URLUtils.PROTOCOL_FILE_PART;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.api.SingletonProvider;
import org.jboss.weld.bootstrap.api.TypeDiscoveryConfiguration;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.configuration.spi.ExternalConfiguration;
import org.jboss.weld.configuration.spi.helpers.ExternalConfigurationBuilder;
import org.jboss.weld.environment.ContainerInstanceFactory;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.jboss.weld.environment.deployment.WeldDeployment;
import org.jboss.weld.environment.deployment.WeldResourceLoader;
import org.jboss.weld.environment.deployment.discovery.ClassPathBeanArchiveScanner;
import org.jboss.weld.environment.deployment.discovery.DiscoveryStrategy;
import org.jboss.weld.environment.deployment.discovery.DiscoveryStrategyFactory;
import org.jboss.weld.environment.logging.CommonLogger;
import org.jboss.weld.environment.se.contexts.ThreadScoped;
import org.jboss.weld.environment.se.logging.WeldSELogger;
import org.jboss.weld.environment.util.BeanArchives;
import org.jboss.weld.environment.util.DevelopmentMode;
import org.jboss.weld.environment.util.Files;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.metadata.BeansXmlImpl;
import org.jboss.weld.metadata.MetadataImpl;
import org.jboss.weld.resources.ClassLoaderResourceLoader;
import org.jboss.weld.resources.spi.ClassFileServices;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.security.GetClassLoaderAction;
import org.jboss.weld.security.GetSystemPropertyAction;
import org.jboss.weld.util.Preconditions;
import org.jboss.weld.util.collections.WeldCollections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * <p>
 * This builder is a preferred method of booting Weld SE container.
 * </p>
 *
 * <p>
 * Typical usage looks like this:
 * </p>
 *
 * <pre>
 * WeldContainer container = new Weld().initialize();
 * container.select(Foo.class).get();
 * container.event().select(Bar.class).fire(new Bar());
 * container.shutdown();
 * </pre>
 *
 * <p>
 * The {@link WeldContainer} implements AutoCloseable:
 * </p>
 *
 * <pre>
 * try (WeldContainer container = new Weld().initialize()) {
 *     container.select(Foo.class).get();
 * }
 * </pre>
 *
 * <p>
 * By default, the discovery is enabled so that all beans from all discovered bean archives are considered. However, it's possible to define a "synthetic" bean
 * archive, or the set of bean classes and enablement respectively:
 * </p>
 *
 * <pre>
 * WeldContainer container = new Weld().beanClasses(Foo.class, Bar.class).alternatives(Bar.class).initialize()) {
 * </pre>
 *
 * <p>
 * Moreover, it's also possible to disable the discovery completely so that only the "synthetic" bean archive is considered:
 * </p>
 *
 * <pre>
 * WeldContainer container = new Weld().disableDiscovery().beanClasses(Foo.class, Bar.class).initialize()) {
 * </pre>
 *
 *
 * <p>
 * In the same manner, it is possible to explicitly declare interceptors, decorators, extensions and Weld-specific options (such as relaxed construction) using
 * the builder.
 * </p>
 *
 * <pre>
 * Weld builder = new Weld()
 *    .disableDiscovery()
 *    .packages(Main.class, Utils.class)
 *    .interceptors(TransactionalInterceptor.class)
 *    .property("org.jboss.weld.construction.relaxed", true);
 * WeldContainer container = builder.initialize();
 * </pre>
 *
 * <p>
 * The builder is reusable which means that it's possible to initialize multiple Weld containers with one builder. However, note that containers must have a
 * unique identifier assigned when running multiple Weld instances at the same time.
 * </p>
 *
 * @author Peter Royle
 * @author Pete Muir
 * @author Ales Justin
 * @author Martin Kouba
 * @see WeldContainer
 */
@Vetoed
public class Weld implements ContainerInstanceFactory {

    /**
     * By default, bean archive isolation is enabled. If set to false, Weld will use a "flat" deployment structure - all bean classes share the same bean
     * archive and all beans.xml descriptors are automatically merged into one.
     * <p>
     * This key can be also used through {@link #property(String, Object)}.
     */
    public static final String ARCHIVE_ISOLATION_SYSTEM_PROPERTY = "org.jboss.weld.se.archive.isolation";

    /**
     * By default, the development mode is disabled. If set to true, the development mode is activated
     * <p>
     * This key can be also used through {@link #property(String, Object)}.
     */
    public static final String DEV_MODE_SYSTEM_PROPERTY = "org.jboss.weld.development";

    /**
     * By default, Weld automatically registers shutdown hook during initialization. If set to false, the registration of a shutdown hook is skipped.
     * <p>
     * This key can be also used through {@link #property(String, Object)}.
     */
    public static final String SHUTDOWN_HOOK_SYSTEM_PROPERTY = "org.jboss.weld.se.shutdownHook";

    /**
     * By default, Weld SE does not support implicit bean archives without beans.xml. If set to true, Weld scans the class path entries and implicit bean
     * archives which don't contain a beans.xml file are also supported.
     * <p>
     * This key can be also used through {@link #property(String, Object)}.
     */
    public static final String SCAN_CLASSPATH_ENTRIES_SYSTEM_PROPERTY = "org.jboss.weld.se.scan.classpath.entries";

    private static final String SYNTHETIC_LOCATION_PREFIX = "synthetic:";

    static {
        if (!(SingletonProvider.instance() instanceof RegistrySingletonProvider)) {
            // make sure RegistrySingletonProvider is used (required for supporting multiple parallel Weld instances)
            SingletonProvider.reset();
            SingletonProvider.initialize(new RegistrySingletonProvider());
        }
    }

    private final Map<String, WeldContainer> initializedContainers;

    private String containerId;

    private boolean discoveryEnabled = true;

    private final Set<String> beanClasses;

    private final List<Metadata<String>> selectedAlternatives;

    private final List<Metadata<String>> selectedAlternativeStereotypes;

    private final List<Metadata<String>> enabledInterceptors;

    private final List<Metadata<String>> enabledDecorators;

    private final Set<Metadata<Extension>> extensions;

    private final Map<String, Object> properties;

    private final Set<PackInfo> packages;

    private ResourceLoader resourceLoader;

    public Weld() {
        this(RegistrySingletonProvider.STATIC_INSTANCE);
    }

    /**
     *
     * @param containerId The container identifier
     * @see Weld#containerId(String)
     */
    public Weld(String containerId) {
        this.containerId = containerId;
        this.initializedContainers = new HashMap<String, WeldContainer>();
        this.beanClasses = new HashSet<String>();
        this.selectedAlternatives = new ArrayList<Metadata<String>>();
        this.selectedAlternativeStereotypes = new ArrayList<Metadata<String>>();
        this.enabledInterceptors = new ArrayList<Metadata<String>>();
        this.enabledDecorators = new ArrayList<Metadata<String>>();
        this.extensions = new HashSet<Metadata<Extension>>();
        this.properties = new HashMap<String, Object>();
        this.packages = new HashSet<PackInfo>();
        this.resourceLoader = new WeldResourceLoader();
    }

    /**
     * Containers must have a unique identifier assigned when running multiple Weld instances at the same time.
     *
     * @param containerId
     * @return self
     */
    public Weld containerId(String containerId) {
        this.containerId = containerId;
        return this;
    }

    /**
     *
     * @return a container identifier
     * @see #containerId(String)
     */
    public String getContainerId() {
        return containerId;
    }

    /**
     * Define the set of bean classes for the synthetic bean archive.
     *
     * @param classes
     * @return self
     */
    public Weld beanClasses(Class<?>... classes) {
        beanClasses.clear();
        for (Class<?> beanClass : classes) {
            addBeanClass(beanClass);
        }
        return this;
    }

    /**
     * Add a bean class to the set of bean classes for the synthetic bean archive.
     *
     * @param beanClass
     * @return self
     */
    public Weld addBeanClass(Class<?> beanClass) {
        beanClasses.add(beanClass.getName());
        return this;
    }

    /**
     * All classes from the packages of the specified classes will be added to the set of bean classes for the synthetic bean archive.
     *
     * <p>
     * Note that the scanning possibilities are limited. Therefore, only directories and jar files from the filesystem are supported.
     * </p>
     *
     * <p>
     * Scanning may also have negative impact on bootstrap performance.
     * </p>
     *
     * @param classes
     * @return self
     */
    public Weld packages(Class<?>... packageClasses) {
        packages.clear();
        addPackages(false, packageClasses);
        return this;
    }

    /**
     * Packages of the specified classes will be scanned and found classes will be added to the set of bean classes for the synthetic bean archive.
     *
     * @param scanRecursively
     * @param packageClasses
     * @return self
     */
    public Weld addPackages(boolean scanRecursively, Class<?>... packageClasses) {
        for (Class<?> packageClass : packageClasses) {
            addPackage(scanRecursively, packageClass);
        }
        return this;
    }

    /**
     * A package of the specified class will be scanned and found classes will be added to the set of bean classes for the synthetic bean archive.
     *
     * @param scanRecursively
     * @param packageClass
     * @return self
     */
    public Weld addPackage(boolean scanRecursively, Class<?> packageClass) {
        packages.add(new PackInfo(packageClass, scanRecursively));
        return this;
    }

    /**
     * Define the set of extensions.
     *
     * @param extensions
     * @return self
     */
    public Weld extensions(Extension... extensions) {
        this.extensions.clear();
        for (Extension extension : extensions) {
            addExtension(extension);
        }
        return this;
    }

    /**
     * Add an extension to the set of extensions.
     *
     * @param extension an extension
     */
    public Weld addExtension(Extension extension) {
        extensions.add(new MetadataImpl<Extension>(extension, SYNTHETIC_LOCATION_PREFIX + extension.getClass().getName()));
        return this;
    }

    /**
     * Enable interceptors for a synthetic bean archive. Interceptor classes are automatically added to the set of bean classes.
     *
     * @param interceptorClasses
     * @return self
     */
    public Weld interceptors(Class<?>... interceptorClasses) {
        enabledInterceptors.clear();
        for (Class<?> interceptorClass : interceptorClasses) {
            addInterceptor(interceptorClass);
        }
        return this;
    }

    /**
     * Add an interceptor class to the list of enabled interceptors for a synthetic bean archive.
     *
     * @param interceptorClass
     * @return self
     */
    public Weld addInterceptor(Class<?> interceptorClass) {
        beanClasses.add(interceptorClass.getName());
        enabledInterceptors.add(syntheticMetadata(interceptorClass));
        return this;
    }

    /**
     * Enable decorators for a synthetic bean archive. Decorator classes are automatically added to the set of bean classes for the synthetic bean archive.
     *
     * @param decoratorClasses
     * @return self
     */
    public Weld decorators(Class<?>... decoratorClasses) {
        enabledDecorators.clear();
        for (Class<?> decoratorClass : decoratorClasses) {
            addDecorator(decoratorClass);
        }
        return this;
    }

    /**
     * Add a decorator class to the list of enabled decorators for a synthetic bean archive.
     *
     * @param decoratorClass
     * @return self
     */
    public Weld addDecorator(Class<?> decoratorClass) {
        beanClasses.add(decoratorClass.getName());
        enabledDecorators.add(syntheticMetadata(decoratorClass));
        return this;
    }

    /**
     * Select alternatives for a synthetic bean archive.
     *
     * @param alternativeClasses
     * @return self
     */
    public Weld alternatives(Class<?>... alternativeClasses) {
        selectedAlternatives.clear();
        for (Class<?> alternativeClass : alternativeClasses) {
            addAlternative(alternativeClass);
        }
        return this;
    }

    /**
     * Add an alternative class to the list of selected alternatives for a synthetic bean archive.
     *
     * @param alternativeClass
     * @return self
     */
    public Weld addAlternative(Class<?> alternativeClass) {
        selectedAlternatives.add(syntheticMetadata(alternativeClass));
        return this;
    }

    /**
     * Select alternative stereotypes for a synthetic bean archive.
     *
     * @param alternativeStereotypeClasses
     * @return self
     */
    @SafeVarargs
    public final Weld alternativeStereotypes(Class<? extends Annotation>... alternativeStereotypeClasses) {
        selectedAlternativeStereotypes.clear();
        for (Class<? extends Annotation> alternativeStereotypeClass : alternativeStereotypeClasses) {
            addAlternativeStereotype(alternativeStereotypeClass);
        }
        return this;
    }

    /**
     * Add an alternative stereotype class to the list of selected alternative stereotypes for a synthetic bean archive.
     *
     * @param alternativeStereotypeClass
     * @return self
     */
    public Weld addAlternativeStereotype(Class<? extends Annotation> alternativeStereotypeClass) {
        selectedAlternativeStereotypes.add(syntheticMetadata(alternativeStereotypeClass));
        return this;
    }

    /**
     * Set the configuration property.
     *
     * @param key
     * @param value
     * @return self
     * @see #ARCHIVE_ISOLATION_SYSTEM_PROPERTY
     * @see #SHUTDOWN_HOOK_SYSTEM_PROPERTY
     * @see #DEV_MODE_SYSTEM_PROPERTY
     * @see ConfigurationKey
     */
    public Weld property(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    /**
     * Reset the synthetic bean archive (bean classes and enablement) and explicitly added extensions.
     *
     * @return self
     */
    public Weld reset() {
        beanClasses.clear();
        packages.clear();
        selectedAlternatives.clear();
        selectedAlternativeStereotypes.clear();
        enabledInterceptors.clear();
        enabledDecorators.clear();
        extensions.clear();
        return this;
    }

    /**
     * Reset all the state, except for initialized containers.
     *
     * @return self
     * @see Weld#reset()
     */
    public Weld resetAll() {
        reset();
        properties.clear();
        enableDiscovery();
        containerId(RegistrySingletonProvider.STATIC_INSTANCE);
        return this;
    }

    /**
     *
     * @return self
     * @see #disableDiscovery()
     */
    public Weld enableDiscovery() {
        this.discoveryEnabled = true;
        return this;
    }

    /**
     * By default, the discovery is enabled. However, it's possible to disable the discovery completely so that only the "synthetic" bean archive is considered.
     *
     * @return self
     */
    public Weld disableDiscovery() {
        this.discoveryEnabled = false;
        return this;
    }

    /**
     *
     * @return <code>true</code> if the discovery is enabled, <code>false</code> otherwise
     * @see #disableDiscovery()
     */
    public boolean isDiscoveryEnabled() {
        return discoveryEnabled;
    }

    /**
     * Bootstraps a new Weld SE container with the current {@link #containerId}.
     * <p/>
     * The container must be shut down properly when an application is stopped. Applications are encouraged to use the try-with-resources statement or invoke
     * {@link WeldContainer#shutdown()} explicitly.
     * <p/>
     * However, a shutdown hook is also registered during initialization so that all running containers are shut down automatically when a program exits or VM
     * is terminated. This means that it's not necessary to implement the shutdown logic in a class where a main method is used to start the container.
     *
     * @return the Weld container
     * @see #enableDiscovery()
     * @see WeldContainer#shutdown()
     */
    public WeldContainer initialize() {
        // If also building a synthetic bean archive or the implicit scan is enabled, the check for beans.xml is not necessary
        if (!isSyntheticBeanArchiveRequired() && !isEnabled(SCAN_CLASSPATH_ENTRIES_SYSTEM_PROPERTY, false) && resourceLoader.getResource(WeldDeployment.BEANS_XML) == null) {
            throw CommonLogger.LOG.missingBeansXml();
        }

        final CDI11Bootstrap bootstrap = new WeldBootstrap();
        final Deployment deployment = createDeployment(resourceLoader, bootstrap);

        final ExternalConfigurationBuilder configurationBuilder = new ExternalConfigurationBuilder()
                // weld-se uses relaxed construction by default
                .add(ConfigurationKey.RELAXED_CONSTRUCTION.get(), true);
        for (Entry<String, Object> property : properties.entrySet()) {
            configurationBuilder.add(property.getKey(), property.getValue());
        }
        deployment.getServices().add(ExternalConfiguration.class, configurationBuilder.build());

        // Set up the container
        bootstrap.startContainer(containerId, Environments.SE, deployment);
        // Start the container
        bootstrap.startInitialization();
        bootstrap.deployBeans();
        bootstrap.validateBeans();
        bootstrap.endInitialization();

        final WeldManager manager = bootstrap.getManager(deployment.loadBeanDeploymentArchive(WeldContainer.class));
        final WeldContainer weldContainer = WeldContainer.initialize(containerId, manager, bootstrap, isEnabled(SHUTDOWN_HOOK_SYSTEM_PROPERTY, true));

        initializedContainers.put(containerId, weldContainer);
        return weldContainer;
    }

    /**
     * Shuts down all the containers initialized by this builder.
     */
    public void shutdown() {
        if (!initializedContainers.isEmpty()) {
            for (WeldContainer container : initializedContainers.values()) {
                container.shutdown();
            }
        }
    }

    /**
     * Set a {@link ClassLoader}. The given {@link ClassLoader} will be scanned automatically for bean archives if scanning is enabled.
     *
     * @param classLoader
     * @return self
     */
    public Weld setClassLoader(ClassLoader classLoader) {
        Preconditions.checkNotNull(classLoader);
        resourceLoader = new ClassLoaderResourceLoader(classLoader);
        return this;
    }

    /**
     * Set a {@link ResourceLoader} used to scan the application for bean archives. If you only want to use a specific {@link ClassLoader} for scanning, use
     * {@link #setClassLoader(ClassLoader)} instead.
     *
     * @param resourceLoader
     * @return self
     * @see #isDiscoveryEnabled()
     */
    public Weld setResourceLoader(ResourceLoader resourceLoader) {
        Preconditions.checkNotNull(resourceLoader);
        this.resourceLoader = resourceLoader;
        return this;
    }

    /**
     * <p>
     * Extensions to Weld SE can subclass and override this method to customize the deployment before weld boots up. For example, to add a custom
     * ResourceLoader, you would subclass Weld like so:
     * </p>
     *
     * <pre>
     * public class MyWeld extends Weld {
     *     protected Deployment createDeployment(ResourceLoader resourceLoader, CDI11Bootstrap bootstrap) {
     *         return super.createDeployment(new MyResourceLoader(), bootstrap);
     *     }
     * }
     * </pre>
     *
     * <p>
     * This could then be used as normal:
     * </p>
     *
     * <pre>
     * WeldContainer container = new MyWeld().initialize();
     * </pre>
     *
     * @param resourceLoader
     * @param bootstrap
     */
    protected Deployment createDeployment(ResourceLoader resourceLoader, CDI11Bootstrap bootstrap) {

        final Iterable<Metadata<Extension>> extensions = getExtensions(WeldResourceLoader.getClassLoader(), bootstrap);
        final TypeDiscoveryConfiguration typeDiscoveryConfiguration = bootstrap.startExtensions(extensions);
        final Deployment deployment;
        final Set<WeldBeanDeploymentArchive> beanArchives = new HashSet<WeldBeanDeploymentArchive>();
        final Map<Class<? extends Service>, Service> additionalServices = new HashMap<>();

        if (discoveryEnabled) {
            DiscoveryStrategy strategy = DiscoveryStrategyFactory.create(resourceLoader, bootstrap,
                    ImmutableSet.<Class<? extends Annotation>> builder().addAll(typeDiscoveryConfiguration.getKnownBeanDefiningAnnotations())
                            // Add ThreadScoped manually as Weld SE doesn't support implicit bean archives without beans.xml
                            .add(ThreadScoped.class).build());
            if (isEnabled(SCAN_CLASSPATH_ENTRIES_SYSTEM_PROPERTY, false)) {
                strategy.setScanner(new ClassPathBeanArchiveScanner(bootstrap));
            }
            beanArchives.addAll(strategy.performDiscovery());
            ClassFileServices classFileServices = strategy.getClassFileServices();
            if (classFileServices != null) {
                additionalServices.put(ClassFileServices.class, classFileServices);
            }
        }

        if (isSyntheticBeanArchiveRequired()) {
            ImmutableSet.Builder<String> beanClassesBuilder = ImmutableSet.builder();
            beanClassesBuilder.addAll(beanClasses);
            beanClassesBuilder.addAll(scanPackages());
            WeldBeanDeploymentArchive syntheticBeanArchive = new WeldBeanDeploymentArchive(WeldDeployment.SYNTHETIC_BDA_ID, beanClassesBuilder.build(),
                    buildSyntheticBeansXml());
            beanArchives.add(syntheticBeanArchive);
        }

        if (beanArchives.isEmpty()) {
            throw WeldSELogger.LOG.weldContainerCannotBeInitializedNoBeanArchivesFound();
        }

        Multimap<String, BeanDeploymentArchive> problems = BeanArchives.findBeanClassesDeployedInMultipleBeanArchives(beanArchives);
        if (!problems.isEmpty()) {
            // Right now, we only log a warning for each bean class deployed in multiple bean archives
            for (Entry<String, Collection<BeanDeploymentArchive>> entry : problems.asMap().entrySet()) {
                WeldSELogger.LOG.beanClassDeployedInMultipleBeanArchives(entry.getKey(), WeldCollections.toMultiRowString(entry.getValue()));
            }
        }

        if (isEnabled(ARCHIVE_ISOLATION_SYSTEM_PROPERTY, true)) {
            deployment = new WeldDeployment(resourceLoader, bootstrap, beanArchives, extensions);
            CommonLogger.LOG.archiveIsolationEnabled();
        } else {
            Set<WeldBeanDeploymentArchive> flatDeploymentArchives = new HashSet<WeldBeanDeploymentArchive>();
            flatDeploymentArchives.add(WeldBeanDeploymentArchive.merge(bootstrap, beanArchives));
            deployment = new WeldDeployment(resourceLoader, bootstrap, flatDeploymentArchives, extensions);
            CommonLogger.LOG.archiveIsolationDisabled();
        }

        deployment.getServices().addAll(additionalServices.entrySet());
        return deployment;
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
            throw CommonLogger.LOG.unableToResolveBean(type, Arrays.asList(bindings));
        }
        CreationalContext<?> cc = manager.createCreationalContext(bean);
        return type.cast(manager.getReference(bean, type, cc));
    }

    private boolean isSyntheticBeanArchiveRequired() {
        return !beanClasses.isEmpty() || !packages.isEmpty();
    }

    private Iterable<Metadata<Extension>> getExtensions(ClassLoader classLoader, Bootstrap bootstrap) {
        Set<Metadata<Extension>> result = new HashSet<Metadata<Extension>>();
        if (discoveryEnabled) {
            Iterables.addAll(result, bootstrap.loadExtensions(classLoader));
        }
        if (!extensions.isEmpty()) {
            result.addAll(extensions);
        }
        // Ensure that WeldSEBeanRegistrant is present
        WeldSEBeanRegistrant weldSEBeanRegistrant = null;
        for (Metadata<Extension> metadata : result) {
            if (metadata.getValue().getClass().getName().equals(WeldSEBeanRegistrant.class.getName())) {
                weldSEBeanRegistrant = (WeldSEBeanRegistrant) metadata.getValue();
                break;
            }
        }

        if (weldSEBeanRegistrant == null) {
            try {
                weldSEBeanRegistrant = SecurityActions.newInstance(WeldSEBeanRegistrant.class);
                result.add(new MetadataImpl<Extension>(weldSEBeanRegistrant, SYNTHETIC_LOCATION_PREFIX + WeldSEBeanRegistrant.class.getName()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (isEnabled(DEV_MODE_SYSTEM_PROPERTY, false)) {
            // The development mode is enabled - register the Probe extension
            result.add(new MetadataImpl<Extension>(DevelopmentMode.getProbeExtension(resourceLoader), "N/A"));
        }
        return result;
    }

    private BeansXml buildSyntheticBeansXml() {
        return new BeansXmlImpl(ImmutableList.copyOf(selectedAlternatives), ImmutableList.copyOf(selectedAlternativeStereotypes),
                ImmutableList.copyOf(enabledDecorators), ImmutableList.copyOf(enabledInterceptors), null, null, BeanDiscoveryMode.ALL, null);
    }

    private MetadataImpl<String> syntheticMetadata(Class<?> clazz) {
        return new MetadataImpl<String>(clazz.getName(), SYNTHETIC_LOCATION_PREFIX + clazz.getName());
    }

    private Set<String> scanPackages() {

        if (packages.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> foundClasses = new HashSet<String>();

        for (PackInfo packInfo : packages) {

            ClassLoader cl = packInfo.getClassLoaderRef().get();
            if (cl == null) {
                continue;
            }
            String packName = packInfo.getPackName();
            URL resourceUrl = cl.getResource(packInfo.getPackClassName().replace('.', '/') + Files.CLASS_FILE_EXTENSION);

            if (resourceUrl != null) {

                WeldSELogger.LOG.scanningPackage(packName, resourceUrl);

                try {
                    URI resourceUri = resourceUrl.toURI();

                    if (PROCOTOL_FILE.equals(resourceUrl.getProtocol())) {
                        // Get the package directory, e.g. "file:///home/weld/org/jboss
                        handleDir(new File(resourceUri).getParentFile(), packInfo.isScanRecursively(), packName, foundClasses);
                    } else if (PROCOTOL_JAR.equals(resourceUrl.getProtocol())) {
                        handleJar(resourceUri, packInfo.isScanRecursively(), packName, foundClasses);
                    } else {
                        WeldSELogger.LOG.resourceUrlProtocolNotSupported(resourceUrl);
                    }

                } catch (URISyntaxException e) {
                    CommonLogger.LOG.couldNotReadResource(resourceUrl, e);
                }
            } else {
                WeldSELogger.LOG.packageNotFound(packName);
            }
        }
        return foundClasses;
    }

    private void handleDir(File packDir, boolean scanRecursively, String packName, Set<String> foundClasses) {
        if (packDir != null && packDir.exists() && packDir.canRead()) {
            for (File file : packDir.listFiles()) {
                if (file.isFile()) {
                    if (file.canRead() && Files.isClass(file.getName())) {
                        foundClasses.add(Files.filenameToClassname(packName + "." + file.getName()));
                    }
                }
                if (file.isDirectory() && scanRecursively) {
                    handleDir(file, scanRecursively, packName + "." + file.getName(), foundClasses);
                }
            }
        }
    }

    private void handleJar(URI resourceUri, boolean scanRecursively, String packName, Set<String> foundClasses) {

        // Currently we only support jar:file
        if (resourceUri.getSchemeSpecificPart().startsWith(PROCOTOL_FILE)) {

            // Get the JAR file path, e.g. "jar:file:/home/duke/duke.jar!/com/foo/Bar" becomes "/home/duke/duke.jar"
            String path = resourceUri.getSchemeSpecificPart().substring(PROTOCOL_FILE_PART.length());
            if (path.lastIndexOf(JAR_URL_SEPARATOR) > 0) {
                path = path.substring(0, path.lastIndexOf(JAR_URL_SEPARATOR));
            }

            JarFile jar = null;
            String packNamePath = packName.replace('.', '/');
            int expectedPartsLength = splitBySlash(packNamePath).length + 1;

            try {
                jar = new JarFile(new File(path));
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (!entry.getName().endsWith(Files.CLASS_FILE_EXTENSION)) {
                        continue;
                    }
                    if (entry.getName().startsWith(packNamePath)) {
                        if (scanRecursively) {
                            foundClasses.add(Files.filenameToClassname(entry.getName()));
                        } else {
                            String[] parts = splitBySlash(entry.getName());
                            if (parts.length == expectedPartsLength) {
                                foundClasses.add(Files.filenameToClassname(entry.getName()));
                            }
                        }
                    }
                }
            } catch (IOException e) {
                CommonLogger.LOG.couldNotReadResource(resourceUri, e);
            } finally {
                if (jar != null) {
                    try {
                        jar.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    private String[] splitBySlash(String value) {
        return value.split("/");
    }

    private boolean isEnabled(String key, boolean defaultValue) {
        Object value = properties.get(key);
        if (value != null) {
            return Boolean.TRUE.equals(value);
        }
        String system = AccessController.doPrivileged(new GetSystemPropertyAction(key));
        if (system != null) {
            return Boolean.valueOf(system);
        }
        return defaultValue;
    }

    private static class PackInfo {

        private final String packName;

        private final String packClassName;

        private final boolean scanRecursively;

        private final WeakReference<ClassLoader> classLoaderRef;

        PackInfo(Class<?> packClass, boolean recursiveScan) {
            this.packName = packClass.getPackage().getName();
            this.packClassName = packClass.getName();
            this.scanRecursively = recursiveScan;
            this.classLoaderRef = new WeakReference<ClassLoader>(AccessController.doPrivileged(new GetClassLoaderAction(packClass)));
        }

        public String getPackName() {
            return packName;
        }

        public String getPackClassName() {
            return packClassName;
        }

        public boolean isScanRecursively() {
            return scanRecursively;
        }

        public WeakReference<ClassLoader> getClassLoaderRef() {
            return classLoaderRef;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((packClassName == null) ? 0 : packClassName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            PackInfo other = (PackInfo) obj;
            if (packClassName == null) {
                if (other.packClassName != null) {
                    return false;
                }
            } else if (!packClassName.equals(other.packClassName)) {
                return false;
            }
            return true;
        }

    }

}
