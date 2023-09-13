/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

import static java.util.Collections.emptyList;
import static org.jboss.weld.config.ConfigurationKey.CONCURRENT_DEPLOYMENT;
import static org.jboss.weld.config.ConfigurationKey.ROLLING_UPGRADES_ID_DELIMITER;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.spi.Context;

import org.jboss.weld.bean.builtin.BeanManagerBean;
import org.jboss.weld.bean.builtin.BeanManagerImplBean;
import org.jboss.weld.bean.builtin.BeanMetadataBean;
import org.jboss.weld.bean.builtin.ContextBean;
import org.jboss.weld.bean.builtin.ConversationBean;
import org.jboss.weld.bean.builtin.DecoratedBeanMetadataBean;
import org.jboss.weld.bean.builtin.DecoratorMetadataBean;
import org.jboss.weld.bean.builtin.EventBean;
import org.jboss.weld.bean.builtin.EventMetadataBean;
import org.jboss.weld.bean.builtin.InjectionPointBean;
import org.jboss.weld.bean.builtin.InstanceBean;
import org.jboss.weld.bean.builtin.InterceptedBeanMetadataBean;
import org.jboss.weld.bean.builtin.InterceptionFactoryBean;
import org.jboss.weld.bean.builtin.InterceptorMetadataBean;
import org.jboss.weld.bean.builtin.RequestContextControllerBean;
import org.jboss.weld.bean.builtin.ee.PrincipalBean;
import org.jboss.weld.bean.proxy.InterceptionFactoryDataCache;
import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.enablement.GlobalEnablementBuilder;
import org.jboss.weld.bootstrap.enablement.ModuleEnablement;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Filter;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.injection.producer.InjectionTargetService;
import org.jboss.weld.interceptor.builder.InterceptorsApiAbstraction;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.ExecutorServices;
import org.jboss.weld.metadata.FilterPredicate;
import org.jboss.weld.metadata.ScanningPredicate;
import org.jboss.weld.module.EjbSupport;
import org.jboss.weld.module.WeldModules;
import org.jboss.weld.persistence.PersistenceApiAbstraction;
import org.jboss.weld.resources.DefaultResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.security.NoopSecurityServices;
import org.jboss.weld.security.spi.SecurityServices;
import org.jboss.weld.util.AnnotationApiAbstraction;
import org.jboss.weld.util.collections.WeldCollections;
import org.jboss.weld.ws.WSApiAbstraction;

/**
 * @author Pete Muir
 * @author Jozef Hartinger
 * @author alesj
 */
public class BeanDeployment {

    private final BeanDeploymentArchive beanDeploymentArchive;
    private final BeanManagerImpl beanManager;
    private final BeanDeployer beanDeployer;
    private final Collection<ContextHolder<? extends Context>> contexts;

    public BeanDeployment(BeanDeploymentArchive beanDeploymentArchive, BeanManagerImpl deploymentManager,
            ServiceRegistry deploymentServices,
            Collection<ContextHolder<? extends Context>> contexts) {
        this(beanDeploymentArchive, deploymentManager, deploymentServices, contexts, false);
    }

    public BeanDeployment(BeanDeploymentArchive beanDeploymentArchive, BeanManagerImpl deploymentManager,
            ServiceRegistry deploymentServices,
            Collection<ContextHolder<? extends Context>> contexts, boolean additionalBeanArchive) {
        this.beanDeploymentArchive = beanDeploymentArchive;

        ServiceRegistry registry = beanDeploymentArchive.getServices();

        ResourceLoader resourceLoader = registry.get(ResourceLoader.class);
        if (resourceLoader == null) {
            resourceLoader = DefaultResourceLoader.INSTANCE;
            registry.add(ResourceLoader.class, resourceLoader);
        }

        ServiceRegistry services = new SimpleServiceRegistry();
        services.addAll(deploymentServices.entrySet());
        services.addAll(registry.entrySet());

        services.add(PersistenceApiAbstraction.class, new PersistenceApiAbstraction(resourceLoader));
        services.add(WSApiAbstraction.class, new WSApiAbstraction(resourceLoader));
        services.add(InterceptorsApiAbstraction.class, new InterceptorsApiAbstraction(resourceLoader));
        services.add(AnnotationApiAbstraction.class, new AnnotationApiAbstraction(resourceLoader));
        this.beanManager = BeanManagerImpl.newManager(deploymentManager,
                BeanDeployments.getFinalId(beanDeploymentArchive.getId(),
                        services.get(WeldConfiguration.class).getStringProperty(ROLLING_UPGRADES_ID_DELIMITER)),
                services);
        services.add(InjectionTargetService.class, new InjectionTargetService(beanManager));
        services.add(InterceptionFactoryDataCache.class, new InterceptionFactoryDataCache(beanManager));

        services.get(WeldModules.class).postBeanArchiveServiceRegistration(services, beanManager, beanDeploymentArchive);
        services.addIfAbsent(EjbSupport.class, EjbSupport.NOOP_IMPLEMENTATION);

        if (services.get(WeldConfiguration.class).getBooleanProperty(CONCURRENT_DEPLOYMENT)
                && services.contains(ExecutorServices.class)) {
            beanDeployer = new ConcurrentBeanDeployer(beanManager, deploymentServices);
        } else {
            beanDeployer = new BeanDeployer(beanManager, deploymentServices);
        }
        beanManager.getServices().get(SpecializationAndEnablementRegistry.class).registerEnvironment(beanManager,
                beanDeployer.getEnvironment(),
                additionalBeanArchive);

        // Must at the Manager bean straight away, as it can be injected during startup!
        beanManager.addBean(new BeanManagerBean(beanManager));
        beanManager.addBean(new BeanManagerImplBean(beanManager));

        this.contexts = contexts;
    }

    public BeanManagerImpl getBeanManager() {
        return beanManager;
    }

    public BeanDeployer getBeanDeployer() {
        return beanDeployer;
    }

    public BeanDeploymentArchive getBeanDeploymentArchive() {
        return beanDeploymentArchive;
    }

    private Predicate<String> createFilter() {
        if (getBeanDeploymentArchive().getBeansXml() == null
                || getBeanDeploymentArchive().getBeansXml().getScanning() == null) {
            return null;
        }
        Function<Metadata<Filter>, Predicate<String>> filterToPredicateFunction = new Function<Metadata<Filter>, Predicate<String>>() {
            final ResourceLoader resourceLoader = beanDeployer.getResourceLoader();

            @Override
            public Predicate<String> apply(Metadata<Filter> from) {
                return new FilterPredicate(from, resourceLoader);
            }
        };

        Collection<Metadata<Filter>> includeFilters;
        if (getBeanDeploymentArchive().getBeansXml().getScanning().getIncludes() != null) {
            includeFilters = getBeanDeploymentArchive().getBeansXml().getScanning().getIncludes();
        } else {
            includeFilters = emptyList();
        }
        Collection<Metadata<Filter>> excludeFilters;
        if (getBeanDeploymentArchive().getBeansXml().getScanning().getExcludes() != null) {
            excludeFilters = getBeanDeploymentArchive().getBeansXml().getScanning().getExcludes();
        } else {
            excludeFilters = emptyList();
        }

        /*
         * Take a copy of the transformed collection, this means that the filter predicate is only built once per filter
         * predicate
         */
        Collection<Predicate<String>> includes = includeFilters.stream().map(filterToPredicateFunction)
                .collect(Collectors.toList());
        Collection<Predicate<String>> excludes = excludeFilters.stream().map(filterToPredicateFunction)
                .collect(Collectors.toList());
        return new ScanningPredicate<String>(includes, excludes);
    }

    public void createClasses() {
        Stream<String> classNames = beanDeploymentArchive.getBeanClasses().stream();
        Collection<Class<?>> loadedClasses = beanDeploymentArchive.getLoadedBeanClasses();

        // filter out names of classes that are available in beanDeploymentArchive.getLoadedBeanClasses()
        if (!loadedClasses.isEmpty()) {
            Set<String> preloadedClassNames = loadedClasses.stream().map(c -> c.getName()).collect(Collectors.toSet());
            classNames = classNames.filter(name -> !preloadedClassNames.contains(name));
        }
        // apply inclusion / exclusion filters
        Predicate<String> filter = createFilter();
        if (filter != null) {
            classNames = classNames.filter(filter);
            loadedClasses = loadedClasses.stream().filter(clazz -> filter.test(clazz.getName())).collect(Collectors.toSet());
        }
        beanDeployer.addLoadedClasses(loadedClasses);
        beanDeployer.addClasses(classNames.collect(Collectors.toSet()));
    }

    /**
     * Initializes module enablement.
     *
     * @see ModuleEnablement
     */
    public void createEnablement() {
        GlobalEnablementBuilder builder = beanManager.getServices().get(GlobalEnablementBuilder.class);
        ModuleEnablement enablement = builder.createModuleEnablement(this);
        beanManager.setEnabled(enablement);

        if (BootstrapLogger.LOG.isDebugEnabled()) {
            BootstrapLogger.LOG.enabledAlternatives(this.beanManager,
                    WeldCollections.toMultiRowString(enablement.getAllAlternatives()));
            BootstrapLogger.LOG.enabledDecorators(this.beanManager,
                    WeldCollections.toMultiRowString(enablement.getDecorators()));
            BootstrapLogger.LOG.enabledInterceptors(this.beanManager,
                    WeldCollections.toMultiRowString(enablement.getInterceptors()));
        }
    }

    public void createTypes() {
        beanDeployer.processAnnotatedTypes();
        beanDeployer.registerAnnotatedTypes();
    }

    public void createBeans(Environment environment) {
        getBeanManager().getServices().get(WeldModules.class).preBeanRegistration(this, environment);

        /*
         * If EjbSupport is installed then SessionBeanAwareInjectionPointBean is used instead
         */
        if (getBeanManager().getServices().get(EjbSupport.class) == EjbSupport.NOOP_IMPLEMENTATION) {
            beanDeployer.addBuiltInBean(new InjectionPointBean(beanManager));
        }

        beanDeployer.addBuiltInBean(new EventMetadataBean(beanManager));
        beanDeployer.addBuiltInBean(new EventBean(beanManager));
        beanDeployer.addBuiltInBean(new InstanceBean(beanManager));
        beanDeployer.addBuiltInBean(new ConversationBean(beanManager));
        beanDeployer.addBuiltInBean(new BeanMetadataBean(beanManager));
        beanDeployer.addBuiltInBean(new InterceptedBeanMetadataBean(beanManager));
        beanDeployer.addBuiltInBean(new DecoratedBeanMetadataBean(beanManager));
        beanDeployer.addBuiltInBean(new InterceptorMetadataBean(beanManager));
        beanDeployer.addBuiltInBean(new DecoratorMetadataBean(beanManager));
        beanDeployer.addBuiltInBean(new InterceptionFactoryBean(beanManager));

        if (beanManager.getServices().getRequired(SecurityServices.class) != NoopSecurityServices.INSTANCE) {
            beanDeployer.addBuiltInBean(new PrincipalBean(beanManager));
        }
        // Register the context beans
        for (ContextHolder<? extends Context> context : contexts) {
            beanDeployer.addBuiltInBean(ContextBean.of(context, beanManager));
        }
        beanDeployer.addBuiltInBean(new RequestContextControllerBean(beanManager));

        if (beanDeploymentArchive.getBeansXml() != null && beanDeploymentArchive.getBeansXml().isTrimmed()) {
            beanDeployer.getEnvironment().trim();
        }
        beanDeployer.createClassBeans();
    }

    public void deploySpecialized(Environment environment) {
        beanDeployer.deploySpecialized();
    }

    public void deployBeans(Environment environment) {
        beanDeployer.deploy();
    }

    public void afterBeanDiscovery(Environment environment) {
        beanDeployer.doAfterBeanDiscovery(beanManager.getBeans());
        beanDeployer.doAfterBeanDiscovery(beanManager.getDecorators());
        beanDeployer.doAfterBeanDiscovery(beanManager.getInterceptors());
        beanDeployer.registerCdiInterceptorsForMessageDrivenBeans();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BeanDeployment ");
        builder.append("[beanDeploymentArchiveId=");
        builder.append(beanDeploymentArchive.getId());
        if (!beanDeploymentArchive.getId().equals(beanManager.getId())) {
            builder.append(", beanManagerId=");
            builder.append(beanManager.getId());
        }
        builder.append("]");
        return builder.toString();
    }

}
