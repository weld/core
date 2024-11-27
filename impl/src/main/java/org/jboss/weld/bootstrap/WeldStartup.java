/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap;

import static org.jboss.weld.config.ConfigurationKey.ROLLING_UPGRADES_ID_DELIMITER;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.event.Startup;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Model;
import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.Interceptor;

import org.jboss.weld.Container;
import org.jboss.weld.ContainerState;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeStore;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeStoreImpl;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.bean.InterceptorImpl;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bean.builtin.BeanManagerBean;
import org.jboss.weld.bean.builtin.BeanManagerImplBean;
import org.jboss.weld.bean.builtin.ContextBean;
import org.jboss.weld.bean.proxy.ProtectionDomainCache;
import org.jboss.weld.bean.proxy.ProxyInstantiator;
import org.jboss.weld.bean.proxy.util.WeldDefaultProxyServices;
import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.TypeDiscoveryConfiguration;
import org.jboss.weld.bootstrap.api.helpers.ServiceRegistries;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.enablement.GlobalEnablementBuilder;
import org.jboss.weld.bootstrap.events.AfterBeanDiscoveryImpl;
import org.jboss.weld.bootstrap.events.AfterDeploymentValidationImpl;
import org.jboss.weld.bootstrap.events.AfterTypeDiscoveryImpl;
import org.jboss.weld.bootstrap.events.BeforeBeanDiscoveryImpl;
import org.jboss.weld.bootstrap.events.ContainerLifecycleEventPreloader;
import org.jboss.weld.bootstrap.events.ContainerLifecycleEvents;
import org.jboss.weld.bootstrap.events.RequiredAnnotationDiscovery;
import org.jboss.weld.bootstrap.spi.CDI11Deployment;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.helpers.MetadataImpl;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.ConfigurationKey.UnusedBeans;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.context.ApplicationContext;
import org.jboss.weld.context.DependentContext;
import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.SingletonContext;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.BoundLiteral;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.context.bound.BoundSessionContext;
import org.jboss.weld.context.unbound.UnboundLiteral;
import org.jboss.weld.contexts.bound.BoundConversationContextImpl;
import org.jboss.weld.contexts.bound.BoundRequestContextImpl;
import org.jboss.weld.contexts.bound.BoundSessionContextImpl;
import org.jboss.weld.contexts.unbound.ApplicationContextImpl;
import org.jboss.weld.contexts.unbound.DependentContextImpl;
import org.jboss.weld.contexts.unbound.RequestContextImpl;
import org.jboss.weld.contexts.unbound.SingletonContextImpl;
import org.jboss.weld.event.ContextEvent;
import org.jboss.weld.event.CurrentEventMetadata;
import org.jboss.weld.event.DefaultObserverNotifierFactory;
import org.jboss.weld.event.GlobalObserverNotifierService;
import org.jboss.weld.executor.ExecutorServicesFactory;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.injection.ResourceInjectionFactory;
import org.jboss.weld.injection.producer.InjectionTargetService;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.logging.VersionLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.BeanManagerLookupService;
import org.jboss.weld.manager.api.ExecutorServices;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.module.ObserverNotifierFactory;
import org.jboss.weld.module.WeldModules;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.DefaultResourceLoader;
import org.jboss.weld.resources.MemberTransformer;
import org.jboss.weld.resources.ReflectionCache;
import org.jboss.weld.resources.ReflectionCacheFactory;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.resources.spi.ClassFileServices;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.security.NoopSecurityServices;
import org.jboss.weld.security.spi.SecurityServices;
import org.jboss.weld.serialization.BeanIdentifierIndex;
import org.jboss.weld.serialization.ContextualStoreImpl;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.serialization.spi.ProxyServices;
import org.jboss.weld.servlet.spi.HttpContextActivationFilter;
import org.jboss.weld.servlet.spi.helpers.AcceptingHttpContextActivationFilter;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.jboss.weld.util.Bindings;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.collections.Iterables;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Common bootstrapping functionality that is run at application startup and
 * detects and register beans
 *
 * @author Pete Muir
 * @author Ales Justin
 * @author Marko Luksa
 */
public class WeldStartup {

    static {
        VersionLogger.LOG.version(Formats.version());
    }

    private BeanManagerImpl deploymentManager;
    private BeanDeploymentArchiveMapping bdaMapping;
    private Collection<ContextHolder<? extends Context>> contexts;
    private List<Metadata<? extends Extension>> extensions;
    private Environment environment;
    private Deployment deployment;
    private DeploymentVisitor deploymentVisitor;
    private final ServiceRegistry initialServices = new SimpleServiceRegistry();
    private String contextId;
    private final Tracker tracker = Trackers.create();

    public WeldStartup() {
    }

    public WeldRuntime startContainer(String contextId, Environment environment, Deployment deployment) {
        if (deployment == null) {
            throw BootstrapLogger.LOG.deploymentRequired();
        }
        tracker.start(Tracker.OP_BOOTSTRAP);
        tracker.start(Tracker.OP_START_CONTAINER);
        checkApiVersion();

        final ServiceRegistry registry = deployment.getServices();

        // initiate part of registry in order to allow access to WeldConfiguration
        new AdditionalServiceLoader(deployment).loadAdditionalServices(registry);

        // Resource Loader has to be loaded prior to WeldConfiguration
        if (!registry.contains(ResourceLoader.class)) {
            registry.add(ResourceLoader.class, DefaultResourceLoader.INSTANCE);
        }

        WeldConfiguration configuration = new WeldConfiguration(registry, deployment);
        registry.add(WeldConfiguration.class, configuration);

        String finalContextId = BeanDeployments.getFinalId(contextId,
                registry.get(WeldConfiguration.class).getStringProperty(ROLLING_UPGRADES_ID_DELIMITER));
        this.contextId = finalContextId;
        this.deployment = deployment;
        this.environment = environment;

        if (this.extensions == null) {
            setExtensions(deployment.getExtensions());
        }
        // Add extension to register built-in components
        this.extensions.add(MetadataImpl.from(new WeldExtension()));

        // Additional Weld extensions
        String vetoTypeRegex = configuration.getStringProperty(ConfigurationKey.VETO_TYPES_WITHOUT_BEAN_DEFINING_ANNOTATION);
        if (!vetoTypeRegex.isEmpty()) {
            this.extensions.add(MetadataImpl.from(new WeldVetoExtension(vetoTypeRegex)));
        }
        if (UnusedBeans.isEnabled(configuration)) {
            this.extensions.add(MetadataImpl.from(new WeldUnusedMetadataExtension()));
        }

        // Finish the rest of registry init, setupInitialServices() requires already changed finalContextId
        tracker.start(Tracker.OP_INIT_SERVICES);
        setupInitialServices();
        registry.addAll(initialServices.entrySet());

        if (!registry.contains(ProxyServices.class)) {
            // add our own default impl that supports class defining
            registry.add(ProxyServices.class, new WeldDefaultProxyServices());
        }
        if (!registry.contains(SecurityServices.class)) {
            registry.add(SecurityServices.class, NoopSecurityServices.INSTANCE);
        }

        addImplementationServices(registry);
        tracker.end();

        verifyServices(registry, environment.getRequiredDeploymentServices(), contextId);
        if (!registry.contains(TransactionServices.class)) {
            BootstrapLogger.LOG.jtaUnavailable();
        }

        this.deploymentManager = BeanManagerImpl.newRootManager(finalContextId, "deployment", registry);

        Container.initialize(finalContextId, deploymentManager,
                ServiceRegistries.unmodifiableServiceRegistry(deployment.getServices()), environment);
        getContainer().setState(ContainerState.STARTING);

        tracker.start(Tracker.OP_CONTEXTS);
        this.contexts = createContexts(registry);
        tracker.end();

        this.bdaMapping = new BeanDeploymentArchiveMapping();
        this.deploymentVisitor = new DeploymentVisitor(deploymentManager, environment, deployment, contexts, bdaMapping);

        if (deployment instanceof CDI11Deployment) {
            registry.add(BeanManagerLookupService.class,
                    new BeanManagerLookupService((CDI11Deployment) deployment, bdaMapping.getBdaToBeanManagerMap()));
        } else {
            BootstrapLogger.LOG.legacyDeploymentMetadataProvided();
        }

        // Read the deployment structure, bdaMapping will be the physical structure
        // as caused by the presence of beans.xml
        tracker.start(Tracker.OP_READ_DEPLOYMENT);
        deploymentVisitor.visit();
        tracker.end();

        WeldRuntime weldRuntime = new WeldRuntime(finalContextId, deploymentManager, bdaMapping.getBdaToBeanManagerMap());
        tracker.end();
        return weldRuntime;
    }

    private void checkApiVersion() {
        if (Bean.class.getInterfaces().length == 1) {
            // this means Bean only extends Contextual - since CDI 1.1 Bean also extends BeanAttributes
            // CDI 1.0 API is detected on classpath - since that would result in obscure exception later, we
            // throw an appropriate exception right now
            throw BootstrapLogger.LOG.cdiApiVersionMismatch();
        }
    }

    private void setupInitialServices() {
        if (initialServices.contains(TypeStore.class)) {
            return;
        }
        // instantiate initial services which we need for this phase
        TypeStore store = new TypeStore();
        SharedObjectCache cache = new SharedObjectCache();
        ReflectionCache reflectionCache = ReflectionCacheFactory.newInstance(store);
        ClassTransformer classTransformer = new ClassTransformer(store, cache, reflectionCache, contextId);
        initialServices.add(TypeStore.class, store);
        initialServices.add(SharedObjectCache.class, cache);
        initialServices.add(ReflectionCache.class, reflectionCache);
        initialServices.add(ClassTransformer.class, classTransformer);
    }

    private void addImplementationServices(ServiceRegistry services) {
        final WeldModules modules = new WeldModules();
        services.add(WeldModules.class, modules);

        final WeldConfiguration configuration = services.get(WeldConfiguration.class);
        services.add(SlimAnnotatedTypeStore.class, new SlimAnnotatedTypeStoreImpl());
        if (services.get(ClassTransformer.class) == null) {
            throw new IllegalStateException(ClassTransformer.class.getSimpleName() + " not installed.");
        }
        services.add(MemberTransformer.class, new MemberTransformer(services.get(ClassTransformer.class)));
        services.add(MetaAnnotationStore.class, new MetaAnnotationStore(services.get(ClassTransformer.class)));

        BeanIdentifierIndex beanIdentifierIndex = null;
        if (configuration.getBooleanProperty(ConfigurationKey.BEAN_IDENTIFIER_INDEX_OPTIMIZATION)) {
            beanIdentifierIndex = new BeanIdentifierIndex();
            services.add(BeanIdentifierIndex.class, beanIdentifierIndex);
        }

        services.add(ContextualStore.class, new ContextualStoreImpl(contextId, beanIdentifierIndex));
        services.add(CurrentInjectionPoint.class, new CurrentInjectionPoint());
        services.add(CurrentEventMetadata.class, new CurrentEventMetadata());
        services.add(SpecializationAndEnablementRegistry.class, new SpecializationAndEnablementRegistry());
        services.add(MissingDependenciesRegistry.class, new MissingDependenciesRegistry());

        /*
         * Setup ExecutorServices
         */
        ExecutorServices executor = services.get(ExecutorServices.class);
        if (executor == null) {
            executor = ExecutorServicesFactory.create(DefaultResourceLoader.INSTANCE, configuration);
            if (executor != null) {
                services.add(ExecutorServices.class, executor);
            }
        }

        services.add(RequiredAnnotationDiscovery.class, new RequiredAnnotationDiscovery(services.get(ReflectionCache.class)));

        services.add(GlobalEnablementBuilder.class, new GlobalEnablementBuilder());
        if (!services.contains(HttpContextActivationFilter.class)) {
            services.add(HttpContextActivationFilter.class, AcceptingHttpContextActivationFilter.INSTANCE);
        }
        services.add(ProtectionDomainCache.class, new ProtectionDomainCache());

        services.add(ProxyInstantiator.class, ProxyInstantiator.Factory.create(configuration));

        services.add(ObserverNotifierFactory.class, DefaultObserverNotifierFactory.INSTANCE);

        services.add(ResourceInjectionFactory.class, new ResourceInjectionFactory());

        modules.postServiceRegistration(contextId, services);

        /*
         * Setup Validator
         */
        Validator validator;
        if (configuration.getBooleanProperty(ConfigurationKey.CONCURRENT_DEPLOYMENT)
                && services.contains(ExecutorServices.class)) {
            validator = new ConcurrentValidator(modules.getPluggableValidators(), executor,
                    UnusedBeans.isEnabled(configuration) ? new ConcurrentHashMap<>() : null);
        } else {
            validator = new Validator(modules.getPluggableValidators(),
                    UnusedBeans.isEnabled(configuration) ? new HashMap<>() : null);
        }
        services.add(Validator.class, validator);

        GlobalObserverNotifierService observerNotificationService = new GlobalObserverNotifierService(services, contextId);
        services.add(GlobalObserverNotifierService.class, observerNotificationService);

        /*
         * Preloader for container lifecycle events
         */
        ContainerLifecycleEventPreloader preloader = null;
        int preloaderThreadPoolSize = configuration.getIntegerProperty(ConfigurationKey.PRELOADER_THREAD_POOL_SIZE);
        if (preloaderThreadPoolSize > 0) {
            preloader = new ContainerLifecycleEventPreloader(preloaderThreadPoolSize,
                    observerNotificationService.getGlobalLenientObserverNotifier());
        }
        services.add(ContainerLifecycleEvents.class,
                new ContainerLifecycleEvents(preloader, services.get(RequiredAnnotationDiscovery.class)));
        if (environment.isEEModulesAware()) {
            services.add(BeanDeploymentModules.class, new BeanDeploymentModules(contextId, services));
        }
    }

    // needs to be resolved once extension beans are deployed
    private void installFastProcessAnnotatedTypeResolver(ServiceRegistry services) {
        ClassFileServices classFileServices = services.get(ClassFileServices.class);
        if (classFileServices != null) {
            final GlobalObserverNotifierService observers = services.get(GlobalObserverNotifierService.class);
            try {
                final FastProcessAnnotatedTypeResolver resolver = new FastProcessAnnotatedTypeResolver(
                        observers.getAllObserverMethods());
                services.add(FastProcessAnnotatedTypeResolver.class, resolver);
            } catch (UnsupportedObserverMethodException e) {
                BootstrapLogger.LOG.notUsingFastResolver(e.getObserver());
                return;
            }
        }
    }

    public void startInitialization() {
        if (deploymentManager == null) {
            throw BootstrapLogger.LOG.managerNotInitialized();
        }
        tracker.start(Tracker.OP_START_INIT);

        Set<BeanDeployment> physicalBeanDeploymentArchives = new HashSet<BeanDeployment>(getBeanDeployments());

        ExtensionBeanDeployer extensionBeanDeployer = new ExtensionBeanDeployer(deploymentManager, deployment, bdaMapping,
                contexts);
        extensionBeanDeployer.addExtensions(extensions);
        extensionBeanDeployer.deployBeans();

        installFastProcessAnnotatedTypeResolver(deploymentManager.getServices());

        // Add the Deployment BeanManager Bean to the Deployment BeanManager
        deploymentManager.addBean(new BeanManagerBean(deploymentManager));
        deploymentManager.addBean(new BeanManagerImplBean(deploymentManager));

        // Re-Read the deployment structure, bdaMapping will be the physical
        // structure, and will add in BDAs for any extensions outside a
        // physical BDA
        deploymentVisitor.visit();

        tracker.start(Tracker.OP_BBD);
        BeforeBeanDiscoveryImpl.fire(deploymentManager, deployment, bdaMapping, contexts);
        tracker.end();

        // for each physical BDA transform its classes into AnnotatedType instances
        for (BeanDeployment beanDeployment : physicalBeanDeploymentArchives) {
            beanDeployment.createClasses();
        }

        // Re-Read the deployment structure, bdaMapping will be the physical
        // structure, extensions and any classes added using addAnnotatedType
        // outside the physical BDA
        deploymentVisitor.visit();

        for (BeanDeployment beanDeployment : getBeanDeployments()) {
            beanDeployment.createTypes();
        }

        tracker.start(Tracker.OP_ATD);
        AfterTypeDiscoveryImpl.fire(deploymentManager, deployment, bdaMapping, contexts);
        tracker.end();

        for (BeanDeployment beanDeployment : getBeanDeployments()) {
            beanDeployment.createEnablement();
        }
        tracker.end();
    }

    public void deployBeans() {
        tracker.start(Tracker.OP_DEPLOY_BEANS);
        for (BeanDeployment deployment : getBeanDeployments()) {
            deployment.createBeans(environment);
        }
        // we must use separate loops, otherwise cyclic specialization would not work
        for (BeanDeployment deployment : getBeanDeployments()) {
            deployment.getBeanDeployer().processClassBeanAttributes();
            deployment.getBeanDeployer().createProducersAndObservers();
        }
        for (BeanDeployment deployment : getBeanDeployments()) {
            deployment.getBeanDeployer().processProducerAttributes();
        }
        for (BeanDeployment beanDeployment : getBeanDeployments()) {
            beanDeployment.deploySpecialized(environment);
        }
        for (BeanDeployment beanDeployment : getBeanDeployments()) {
            beanDeployment.deployBeans(environment);
        }

        getContainer().setState(ContainerState.DISCOVERED);

        // Flush caches for BeanManager.getBeans() to be usable in ABD (WELD-1729)
        flushCaches();

        tracker.start(Tracker.OP_ABD);
        AfterBeanDiscoveryImpl.fire(deploymentManager, deployment, bdaMapping, contexts);
        tracker.end();

        // Extensions may have registered beans / observers. We need to flush caches.
        flushCaches();

        // If needed, recreate enablement once again - extensions may have registered interceptors, decorators and alternatives
        if (deployment.getServices().getRequired(GlobalEnablementBuilder.class).isDirty()) {
            for (BeanDeployment beanDeployment : getBeanDeployments()) {
                beanDeployment.createEnablement();
            }
        }

        // Re-read the deployment structure, bdaMapping will be the physical
        // structure, extensions, classes, and any beans added using addBean
        // outside the physical structure
        deploymentVisitor.visit();

        for (BeanDeployment beanDeployment : getBeanDeployments()) {
            beanDeployment.getBeanManager().getServices().get(InjectionTargetService.class).initialize();
            beanDeployment.afterBeanDiscovery(environment);
        }
        getContainer().putBeanDeployments(bdaMapping);
        getContainer().setState(ContainerState.DEPLOYED);
        tracker.end();
    }

    public void validateBeans() {
        BootstrapLogger.LOG.validatingBeans();
        tracker.start(Tracker.OP_VALIDATE_BEANS);
        try {
            for (BeanDeployment beanDeployment : getBeanDeployments()) {
                BeanManagerImpl beanManager = beanDeployment.getBeanManager();
                beanManager.getBeanResolver().clear();
                deployment.getServices().get(Validator.class).validateDeployment(beanManager, beanDeployment);
                beanManager.getServices().get(InjectionTargetService.class).validate();
            }
        } catch (Exception e) {
            validationFailed(e);
            throw e;
        }
        getContainer().setState(ContainerState.VALIDATED);
        tracker.start(Tracker.OP_ADV);
        AfterDeploymentValidationImpl.fire(deploymentManager);

        final BeanIdentifierIndex index = deploymentManager.getServices().get(BeanIdentifierIndex.class);
        if (index != null) {
            // Build a special index of bean identifiers
            index.build(getBeansForBeanIdentifierIndex());
        }

        // feed BeanDeploymentModule registry
        final BeanDeploymentModules modules = deploymentManager.getServices().get(BeanDeploymentModules.class);
        if (modules != null) {
            modules.processBeanDeployments(getBeanDeployments());
            BootstrapLogger.LOG.debugv("EE modules: {0}", modules);
        }

        tracker.end();
        tracker.end();
    }

    public void endInitialization() {
        tracker.start(Tracker.OP_END_INIT);

        // Register the managers so external requests can handle them
        // clear the TypeSafeResolvers, so data that is only used at startup
        // is not kept around using up memory
        flushCaches();
        deploymentManager.getServices().cleanupAfterBoot();
        deploymentManager.cleanupAfterBoot();
        for (BeanDeployment beanDeployment : getBeanDeployments()) {
            BeanManagerImpl beanManager = beanDeployment.getBeanManager();
            beanManager.getInterceptorMetadataReader().cleanAfterBoot();
            beanManager.getServices().cleanupAfterBoot();
            beanManager.cleanupAfterBoot();
            // for safety sake perform after boot cleanup on all services in BDA
            beanDeployment.getBeanDeploymentArchive().getServices().cleanupAfterBoot();
            // clean up beans
            for (Bean<?> bean : beanManager.getBeans()) {
                if (bean instanceof RIBean<?>) {
                    RIBean<?> riBean = (RIBean<?>) bean;
                    riBean.cleanupAfterBoot();
                }
            }
            // clean up decorators
            for (Decorator<?> decorator : beanManager.getDecorators()) {
                if (decorator instanceof DecoratorImpl<?>) {
                    Reflections.<DecoratorImpl<?>> cast(decorator).cleanupAfterBoot();
                }
            }
            // clean up interceptors
            for (Interceptor<?> interceptor : beanManager.getInterceptors()) {
                if (interceptor instanceof InterceptorImpl<?>) {
                    Reflections.<InterceptorImpl<?>> cast(interceptor).cleanupAfterBoot();
                }
            }
        }
        for (BeanDeployment beanDeployment : getBeanDeployments()) {
            beanDeployment.getBeanDeployer().cleanup();
        }

        // Perform additional cleanup if removing unused beans
        if (UnusedBeans.isEnabled(deploymentManager.getServices().get(WeldConfiguration.class))) {
            deploymentManager.getBeanResolver().clear();
            for (BeanDeployment beanDeployment : getBeanDeployments()) {
                beanDeployment.getBeanManager().getBeanResolver().clear();
            }
            deploymentManager.getServices().get(Validator.class).clearResolved();
            deploymentManager.getServices().get(ClassTransformer.class).cleanupAfterBoot();
        }

        getContainer().setState(ContainerState.INITIALIZED);

        final BeanDeploymentModules modules = deploymentManager.getServices().get(BeanDeploymentModules.class);
        if (modules != null) {
            // fire @Initialized(ApplicationScoped.class) for non-web modules
            // web modules are handled by HttpContextLifecycle
            for (BeanDeploymentModule module : modules) {
                if (!module.isWebModule()) {
                    module.fireEvent(Object.class, ContextEvent.APPLICATION_INITIALIZED, Initialized.Literal.APPLICATION);
                    // Fire Startup event for all non-web modules if required
                    // web modules have to be handled alongside @Initialized(AppScoped) fired there to make sure the ordering fits
                    if (environment.automaticallyHandleStartupShutdownEvents()) {
                        module.fireEvent(Startup.class, new Startup(), Any.Literal.INSTANCE);
                    }
                }
            }
        }
        tracker.close();
    }

    private void flushCaches() {
        deploymentManager.getBeanResolver().clear();
        deploymentManager.getAccessibleLenientObserverNotifier().clear();
        deploymentManager.getGlobalStrictObserverNotifier().clear();
        deploymentManager.getGlobalLenientObserverNotifier().clear();
        deploymentManager.getDecoratorResolver().clear();
        deploymentManager.getInterceptorResolver().clear();
        deploymentManager.getNameBasedResolver().clear();
        for (BeanDeployment beanDeployment : getBeanDeployments()) {
            BeanManagerImpl beanManager = beanDeployment.getBeanManager();
            beanManager.getBeanResolver().clear();
            beanManager.getAccessibleLenientObserverNotifier().clear();
            beanManager.getDecoratorResolver().clear();
            beanManager.getInterceptorResolver().clear();
            beanManager.getNameBasedResolver().clear();
        }
    }

    private Collection<BeanDeployment> getBeanDeployments() {
        return bdaMapping.getBeanDeployments();
    }

    private Container getContainer() {
        return Container.instance(contextId);
    }

    protected Collection<ContextHolder<? extends Context>> createContexts(ServiceRegistry services) {
        List<ContextHolder<? extends Context>> contexts = new ArrayList<ContextHolder<? extends Context>>();

        BeanIdentifierIndex beanIdentifierIndex = services.get(BeanIdentifierIndex.class);

        /*
         * Register a full set of bound and unbound contexts. Although we may not use all of
         * these (e.g. if we are running in a servlet environment) they may be
         * useful for an application.
         */
        Set<Annotation> boundQualifires = ImmutableSet.<Annotation> builder().addAll(Bindings.DEFAULT_QUALIFIERS)
                .add(BoundLiteral.INSTANCE).build();
        Set<Annotation> unboundQualifiers = ImmutableSet.<Annotation> builder().addAll(Bindings.DEFAULT_QUALIFIERS)
                .add(UnboundLiteral.INSTANCE).build();
        contexts.add(new ContextHolder<ApplicationContext>(new ApplicationContextImpl(contextId), ApplicationContext.class,
                unboundQualifiers));
        contexts.add(new ContextHolder<SingletonContext>(new SingletonContextImpl(contextId), SingletonContext.class,
                unboundQualifiers));
        contexts.add(new ContextHolder<BoundSessionContext>(new BoundSessionContextImpl(contextId, beanIdentifierIndex),
                BoundSessionContext.class, boundQualifires));
        contexts.add(new ContextHolder<BoundConversationContext>(new BoundConversationContextImpl(contextId, services),
                BoundConversationContext.class, boundQualifires));
        contexts.add(new ContextHolder<BoundRequestContext>(new BoundRequestContextImpl(contextId), BoundRequestContext.class,
                boundQualifires));
        contexts.add(
                new ContextHolder<RequestContext>(new RequestContextImpl(contextId), RequestContext.class, unboundQualifiers));
        contexts.add(new ContextHolder<DependentContext>(new DependentContextImpl(services.get(ContextualStore.class)),
                DependentContext.class, unboundQualifiers));

        services.get(WeldModules.class).postContextRegistration(contextId, services, contexts);

        /*
         * Register the contexts with the bean manager and add the beans to the
         * deployment manager so that they are easily accessible (contexts are app
         * scoped)
         */
        for (ContextHolder<? extends Context> context : contexts) {
            deploymentManager.addContext(context.getContext());
            deploymentManager.addBean(ContextBean.of(context, deploymentManager));
        }

        return contexts;
    }

    protected static void verifyServices(ServiceRegistry services, Set<Class<? extends Service>> requiredServices,
            Object target) {
        for (Class<? extends Service> serviceType : requiredServices) {
            if (!services.contains(serviceType)) {
                throw BootstrapLogger.LOG.unspecifiedRequiredService(serviceType.getName(), target);
            }
        }
    }

    public TypeDiscoveryConfiguration startExtensions(Iterable<Metadata<Extension>> extensions) {
        setExtensions(extensions);
        // TODO WELD-1624 Weld should fire BeforeBeanDiscovery to allow extensions to register additional scopes
        final Set<Class<? extends Annotation>> beanDefiningAnnotations = ImmutableSet.of(
                // built-in scopes
                Dependent.class, RequestScoped.class, ConversationScoped.class, SessionScoped.class, ApplicationScoped.class,
                jakarta.interceptor.Interceptor.class, jakarta.decorator.Decorator.class,
                // built-in stereotype
                Model.class,
                // meta-annotations
                NormalScope.class, Stereotype.class);
        return new TypeDiscoveryConfigurationImpl(beanDefiningAnnotations);
    }

    /**
     * Right now, only session and conversation scoped beans (except for built-in beans) are taken into account.
     *
     * @return the set of beans the index should be built from
     */
    private Set<Bean<?>> getBeansForBeanIdentifierIndex() {
        Set<Bean<?>> beans = new HashSet<Bean<?>>();
        for (BeanDeployment beanDeployment : getBeanDeployments()) {
            for (Bean<?> bean : beanDeployment.getBeanManager().getBeans()) {
                if (!(bean instanceof AbstractBuiltInBean<?>)
                        && (bean.getScope().equals(SessionScoped.class) || bean.getScope().equals(ConversationScoped.class))) {
                    beans.add(bean);
                }
            }
        }
        return beans;
    }

    private void setExtensions(Iterable<Metadata<Extension>> extensions) {
        this.extensions = new ArrayList<Metadata<? extends Extension>>();
        Iterables.addAll(this.extensions, extensions);
    }

    BeanManagerImpl getDeploymentManager() {
        return deploymentManager;
    }

    BeanDeploymentArchiveMapping getBdaMapping() {
        return bdaMapping;
    }

    Collection<ContextHolder<? extends Context>> getContexts() {
        return contexts;
    }

    Deployment getDeployment() {
        return deployment;
    }

    private void validationFailed(Exception failure) {
        for (BeanDeployment beanDeployment : getBeanDeployments()) {
            beanDeployment.getBeanManager().validationFailed(failure, environment);
        }
    }

}
