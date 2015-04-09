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

import static org.jboss.weld.config.ConfigurationKey.EXECUTOR_THREAD_POOL_TYPE;
import static org.jboss.weld.environment.util.URLUtils.JAR_URL_SEPARATOR;
import static org.jboss.weld.environment.util.URLUtils.PROCOTOL_FILE;
import static org.jboss.weld.environment.util.URLUtils.PROCOTOL_JAR;
import static org.jboss.weld.environment.util.URLUtils.PROTOCOL_FILE_PART;
import static org.jboss.weld.executor.ExecutorServicesFactory.ThreadPoolType.COMMON;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.jboss.weld.bootstrap.api.SingletonProvider;
import org.jboss.weld.bootstrap.api.TypeDiscoveryConfiguration;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.configuration.spi.ExternalConfiguration;
import org.jboss.weld.configuration.spi.helpers.ExternalConfigurationBuilder;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.jboss.weld.environment.deployment.WeldDeployment;
import org.jboss.weld.environment.deployment.WeldResourceLoader;
import org.jboss.weld.environment.deployment.discovery.DiscoveryStrategy;
import org.jboss.weld.environment.deployment.discovery.DiscoveryStrategyFactory;
import org.jboss.weld.environment.logging.CommonLogger;
import org.jboss.weld.environment.se.contexts.ThreadScoped;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.jboss.weld.environment.se.logging.WeldSELogger;
import org.jboss.weld.environment.util.Files;
import org.jboss.weld.literal.InitializedLiteral;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.metadata.BeansXmlImpl;
import org.jboss.weld.metadata.MetadataImpl;
import org.jboss.weld.resources.spi.ClassFileServices;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.security.GetClassLoaderAction;
import org.jboss.weld.security.GetSystemPropertyAction;
import org.jboss.weld.util.collections.ImmutableList;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.collections.Iterables;

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
public class Weld {

    public static final String ARCHIVE_ISOLATION_SYSTEM_PROPERTY = "org.jboss.weld.se.archive.isolation";

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

    private final Set<Class<?>> packageClasses;

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
        this.packageClasses = new HashSet<Class<?>>();
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
        this.beanClasses.clear();
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
        this.beanClasses.add(beanClass.getName());
        return this;
    }

    /**
     * All classes from the packages of the specified classes will be added to the set of bean classes for the synthetic bean archive.
     *
     * <p>
     * Note that the scanning possibilities are limited. Therefore, only directories and jar files from the filesystem are supported. Scanning may also have
     * negative impact on bootstrap performance.
     * </p>
     *
     * @param classes
     * @return self
     */
    public Weld packages(Class<?>... classes) {
        this.packageClasses.clear();
        for (Class<?> packageClass : classes) {
            this.packageClasses.add(packageClass);
        }
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
            beanClasses.add(interceptorClass.getName());
            enabledInterceptors.add(syntheticMetadata(interceptorClass));
        }
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
            beanClasses.add(decoratorClass.getName());
            enabledDecorators.add(syntheticMetadata(decoratorClass));
        }
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
            selectedAlternatives.add(syntheticMetadata(alternativeClass));
        }
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
        for (Class<?> alternativeStereotypClass : alternativeStereotypeClasses) {
            selectedAlternativeStereotypes.add(syntheticMetadata(alternativeStereotypClass));
        }
        return this;
    }

    /**
     * Set the configuration property.
     *
     * @param key
     * @param value
     * @return self
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
        packageClasses.clear();
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
     * Bootstraps a Weld SE container with the current {@link #containerId}.
     *
     * @return the Weld container
     * @see #setDiscoveryEnabled(boolean)
     */
    public WeldContainer initialize() {
        final ResourceLoader resourceLoader = new WeldResourceLoader();

        // If also building a synthetic bean archive the check for beans.xml is not necessary
        if (!isSyntheticBeanArchiveRequired() && resourceLoader.getResource(WeldDeployment.BEANS_XML) == null) {
            throw CommonLogger.LOG.missingBeansXml();
        }

        final CDI11Bootstrap bootstrap = new WeldBootstrap();
        final Deployment deployment = createDeployment(resourceLoader, bootstrap);

        final ExternalConfigurationBuilder configurationBuilder = new ExternalConfigurationBuilder()
        // weld-se uses CommonForkJoinPoolExecutorServices by default
                .add(EXECUTOR_THREAD_POOL_TYPE.get(), COMMON.toString())
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
        final WeldContainer weldContainer = WeldContainer.initialize(containerId, manager, bootstrap);

        weldContainer.event().select(ContainerInitialized.class, InitializedLiteral.APPLICATION).fire(new ContainerInitialized(containerId));

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
        final DiscoveryStrategy strategy;

        if (discoveryEnabled) {
            strategy = DiscoveryStrategyFactory.create(resourceLoader, bootstrap,
                    ImmutableSet.<Class<? extends Annotation>> builder().addAll(typeDiscoveryConfiguration.getKnownBeanDefiningAnnotations())
                    // Add ThreadScoped manually as Weld SE doesn't support implicit bean archives without beans.xml
                            .add(ThreadScoped.class).build());
            beanArchives.addAll(strategy.performDiscovery());
        } else {
            strategy = null;
        }

        if (isSyntheticBeanArchiveRequired()) {
            ImmutableSet.Builder<String> beanClassesBuilder = ImmutableSet.builder();
            beanClassesBuilder.addAll(beanClasses);
            beanClassesBuilder.addAll(scanPackages());
            WeldBeanDeploymentArchive syntheticBeanArchive = new WeldBeanDeploymentArchive(WeldDeployment.SYNTHETIC_BDA_ID, beanClassesBuilder.build(),
                    buildSyntheticBeansXml());
            beanArchives.add(syntheticBeanArchive);
        }

        String isolation = AccessController.doPrivileged(new GetSystemPropertyAction(ARCHIVE_ISOLATION_SYSTEM_PROPERTY));

        if (isolation != null && Boolean.valueOf(isolation).equals(Boolean.FALSE)) {
            Set<WeldBeanDeploymentArchive> flatDeploymentArchives = new HashSet<WeldBeanDeploymentArchive>();
            flatDeploymentArchives.add(WeldBeanDeploymentArchive.merge(bootstrap, beanArchives));
            deployment = new WeldDeployment(resourceLoader, bootstrap, flatDeploymentArchives, extensions);
            CommonLogger.LOG.archiveIsolationDisabled();
        } else {
            deployment = new WeldDeployment(resourceLoader, bootstrap, beanArchives, extensions);
            CommonLogger.LOG.archiveIsolationEnabled();
        }

        if (strategy != null && strategy.getClassFileServices() != null) {
            deployment.getServices().add(ClassFileServices.class, strategy.getClassFileServices());
        }
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
        return !beanClasses.isEmpty() || !packageClasses.isEmpty();
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
        for (Metadata<Extension> metadata : result) {
            if (metadata.getValue().getClass().getName().equals(WeldSEBeanRegistrant.class.getName())) {
                return result;
            }
        }
        try {
            result.add(new MetadataImpl<Extension>(SecurityActions.newInstance(WeldSEBeanRegistrant.class), SYNTHETIC_LOCATION_PREFIX
                    + WeldSEBeanRegistrant.class.getName()));
        } catch (Exception e) {
            throw new RuntimeException(e);
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

        if (packageClasses.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> foundClasses = new HashSet<String>();

        for (Class<?> packClass : packageClasses) {

            ClassLoader cl = AccessController.doPrivileged(new GetClassLoaderAction(packClass));
            String packName = packClass.getPackage().getName();
            URL resource = cl.getResource(packClass.getName().replace('.', '/') + Files.CLASS_FILE_EXTENSION);

            if (resource != null) {

                WeldSELogger.LOG.scanningPackage(packName, resource);

                URI uri = null;
                try {
                    uri = resource.toURI();
                } catch (URISyntaxException e) {
                    CommonLogger.LOG.couldNotReadResource(resource, e);
                }

                if (PROCOTOL_FILE.equals(resource.getProtocol())) {
                    // Get the package directory, e.g. "file:///home/weld/org/jboss
                    File packDir = new File(uri).getParentFile();
                    if (packDir != null && packDir.exists() && packDir.canRead()) {
                        for (File file : packDir.listFiles()) {
                            if (file.isFile() && file.exists() && Files.isClass(file.getName())) {
                                foundClasses.add(Files.filenameToClassname(packName + "." + file.getName()));
                            }
                        }
                    }
                } else if (PROCOTOL_JAR.equals(resource.getProtocol())) {

                    // Currently we only support jar:file
                    if (uri.getSchemeSpecificPart().startsWith(PROCOTOL_FILE)) {

                        // Get the JAR file path, e.g. "jar:file:/home/duke/duke.jar!/com/foo/Bar" becomes "/home/duke/duke.jar"
                        String path = uri.getSchemeSpecificPart().substring(PROTOCOL_FILE_PART.length());
                        if (path.lastIndexOf(JAR_URL_SEPARATOR) > 0) {
                            path = path.substring(0, path.lastIndexOf(JAR_URL_SEPARATOR));
                        }

                        JarFile jar = null;
                        String packNamePath = packName.replace('.', '/');

                        try {
                            jar = new JarFile(new File(path));
                            Enumeration<JarEntry> entries = jar.entries();
                            while (entries.hasMoreElements()) {
                                JarEntry entry = entries.nextElement();
                                if (entry.getName().endsWith(Files.CLASS_FILE_EXTENSION) && entry.getName().startsWith(packNamePath)) {
                                    foundClasses.add(Files.filenameToClassname(entry.getName()));
                                }
                            }
                        } catch (IOException e) {
                            CommonLogger.LOG.couldNotReadResource(resource, e);
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
            } else {
                WeldSELogger.LOG.packageNotFound(packName);
            }
        }
        return foundClasses;
    }

}
