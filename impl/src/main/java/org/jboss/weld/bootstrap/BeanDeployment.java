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

import static org.jboss.weld.config.ConfigurationKey.CONCURRENT_DEPLOYMENT;
import static org.jboss.weld.config.ConfigurationKey.ROLLING_UPGRADES_ID_DELIMITER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.enterprise.context.spi.Context;

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
import org.jboss.weld.bean.builtin.InterceptorMetadataBean;
import org.jboss.weld.bean.builtin.ee.HttpServletRequestBean;
import org.jboss.weld.bean.builtin.ee.HttpSessionBean;
import org.jboss.weld.bean.builtin.ee.PrincipalBean;
import org.jboss.weld.bean.builtin.ee.ServletContextBean;
import org.jboss.weld.bean.builtin.ee.UserTransactionBean;
import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.enablement.GlobalEnablementBuilder;
import org.jboss.weld.bootstrap.enablement.ModuleEnablement;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.Filter;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.TrimmableBeansXml;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.ejb.EJBApiAbstraction;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.injection.producer.InjectionTargetService;
import org.jboss.weld.interceptor.builder.InterceptorsApiAbstraction;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.ExecutorServices;
import org.jboss.weld.metadata.FilterPredicate;
import org.jboss.weld.metadata.ScanningPredicate;
import org.jboss.weld.persistence.PersistenceApiAbstraction;
import org.jboss.weld.resources.DefaultResourceLoader;
import org.jboss.weld.resources.WeldClassLoaderResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.security.spi.SecurityServices;
import org.jboss.weld.servlet.ServletApiAbstraction;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.jboss.weld.util.AnnotationApiAbstraction;
import org.jboss.weld.util.Function;
import org.jboss.weld.util.JtaApiAbstraction;
import org.jboss.weld.util.Predicate;
import org.jboss.weld.util.collections.WeldCollections;
import org.jboss.weld.util.reflection.Reflections;
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

    public BeanDeployment(BeanDeploymentArchive beanDeploymentArchive, BeanManagerImpl deploymentManager, ServiceRegistry deploymentServices,
            Collection<ContextHolder<? extends Context>> contexts) {
        this(beanDeploymentArchive, deploymentManager, deploymentServices, contexts, false);
    }

    public BeanDeployment(BeanDeploymentArchive beanDeploymentArchive, BeanManagerImpl deploymentManager, ServiceRegistry deploymentServices, Collection<ContextHolder<? extends Context>> contexts, boolean additionalBeanArchive) {
        this.beanDeploymentArchive = beanDeploymentArchive;
        EjbDescriptors ejbDescriptors = new EjbDescriptors();

        ServiceRegistry registry = beanDeploymentArchive.getServices();
        registry.add(EjbDescriptors.class, ejbDescriptors);

        ResourceLoader resourceLoader = registry.get(ResourceLoader.class);
        if (resourceLoader == null) {
            resourceLoader = DefaultResourceLoader.INSTANCE;
            registry.add(ResourceLoader.class, resourceLoader);
        }

        ServiceRegistry services = new SimpleServiceRegistry();
        services.addAll(deploymentServices.entrySet());
        services.addAll(registry.entrySet());

        services.add(EJBApiAbstraction.class, new EJBApiAbstraction(resourceLoader));
        services.add(PersistenceApiAbstraction.class, new PersistenceApiAbstraction(resourceLoader));
        services.add(WSApiAbstraction.class, new WSApiAbstraction(resourceLoader));
        services.add(JtaApiAbstraction.class, new JtaApiAbstraction(resourceLoader));
        services.add(InterceptorsApiAbstraction.class, new InterceptorsApiAbstraction(resourceLoader));
        services.add(AnnotationApiAbstraction.class, new AnnotationApiAbstraction(resourceLoader));
        services.add(ServletApiAbstraction.class, new ServletApiAbstraction(resourceLoader));
        this.beanManager = BeanManagerImpl.newManager(deploymentManager, BeanDeployments.getFinalId(beanDeploymentArchive.getId(),
            services.get(WeldConfiguration.class).getStringProperty(ROLLING_UPGRADES_ID_DELIMITER)), services);
        services.add(InjectionTargetService.class, new InjectionTargetService(beanManager));
        if (beanManager.getServices().contains(EjbServices.class)) {
            // Must populate EJB cache first, as we need it to detect whether a
            // bean is an EJB!
            ejbDescriptors.addAll(beanDeploymentArchive.getEjbs());
        }

        if (services.get(WeldConfiguration.class).getBooleanProperty(CONCURRENT_DEPLOYMENT) && services.contains(ExecutorServices.class)) {
            beanDeployer = new ConcurrentBeanDeployer(beanManager, ejbDescriptors, deploymentServices);
        } else {
            beanDeployer = new BeanDeployer(beanManager, ejbDescriptors, deploymentServices);
        }
        beanManager.getServices().get(SpecializationAndEnablementRegistry.class).registerEnvironment(beanManager, beanDeployer.getEnvironment(), additionalBeanArchive);

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

    protected Iterable<String> obtainClasses() {
        if (getBeanDeploymentArchive().getBeansXml() != null && getBeanDeploymentArchive().getBeansXml().getBeanDiscoveryMode().equals(BeanDiscoveryMode.NONE)) {
            // if the integrator for some reason ignored the "none" flag make sure we do not process the archive
            return Collections.emptySet();
        }
        Function<Metadata<Filter>, Predicate<String>> filterToPredicateFunction = new Function<Metadata<Filter>, Predicate<String>>() {

            final ResourceLoader resourceLoader = beanDeployer.getResourceLoader();

            @Override
            public Predicate<String> apply(Metadata<Filter> from) {
                return new FilterPredicate(from, resourceLoader);
            }

        };
        Collection<String> classNames;
        if (getBeanDeploymentArchive().getBeansXml() != null && getBeanDeploymentArchive().getBeansXml().getScanning() != null) {
            Collection<Metadata<Filter>> includeFilters;
            if (getBeanDeploymentArchive().getBeansXml().getScanning().getIncludes() != null) {
                includeFilters = getBeanDeploymentArchive().getBeansXml().getScanning().getIncludes();
            } else {
                includeFilters = Collections.emptyList();
            }
            Collection<Metadata<Filter>> excludeFilters;
            if (getBeanDeploymentArchive().getBeansXml().getScanning().getExcludes() != null) {
                excludeFilters = getBeanDeploymentArchive().getBeansXml().getScanning().getExcludes();
            } else {
                excludeFilters = Collections.emptyList();
            }
            /*
            * Take a copy of the transformed collection, this means that the
            * filter predicate is only built once per filter predicate
            */
            Collection<Predicate<String>> includes;
            if (includeFilters.isEmpty()) {
                includes = Collections.emptyList();
            } else {
                includes = new ArrayList<Predicate<String>>(includeFilters.size());
                for (Metadata<Filter> includeFilter : includeFilters) {
                    includes.add(filterToPredicateFunction.apply(includeFilter));
                }
            }
            Collection<Predicate<String>> excludes;
            if (excludeFilters.isEmpty()) {
                excludes = Collections.emptyList();
            } else {
                excludes = new ArrayList<Predicate<String>>(excludeFilters.size());
                for (Metadata<Filter> excludeFilter : excludeFilters) {
                    excludes.add(filterToPredicateFunction.apply(excludeFilter));
                }
            }
            Predicate<String> filters = new ScanningPredicate<String>(includes, excludes);
            classNames = new HashSet<>();
            for (String beanClass : beanDeploymentArchive.getBeanClasses()) {
                if(filters.test(beanClass)) {
                    classNames.add(beanClass);
                }
            }
        } else {
            classNames = beanDeploymentArchive.getBeanClasses();
        }
        return classNames;
    }

    public void createClasses() {
        beanDeployer.addClasses(obtainClasses());
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
            BootstrapLogger.LOG.enabledAlternatives(this.beanManager, WeldCollections.toMultiRowString(enablement.getAllAlternatives()));
            BootstrapLogger.LOG.enabledDecorators(this.beanManager, WeldCollections.toMultiRowString(enablement.getDecorators()));
            BootstrapLogger.LOG.enabledInterceptors(this.beanManager, WeldCollections.toMultiRowString(enablement.getInterceptors()));
        }
    }

    public void createTypes() {
        beanDeployer.processAnnotatedTypes();
        beanDeployer.registerAnnotatedTypes();
    }

    public void createBeans(Environment environment) {
        beanDeployer.addBuiltInBean(new InjectionPointBean(beanManager));
        beanDeployer.addBuiltInBean(new EventMetadataBean(beanManager));
        beanDeployer.addBuiltInBean(new EventBean(beanManager));
        beanDeployer.addBuiltInBean(new InstanceBean(beanManager));
        beanDeployer.addBuiltInBean(new ConversationBean(beanManager));
        beanDeployer.addBuiltInBean(new BeanMetadataBean(beanManager));
        beanDeployer.addBuiltInBean(new InterceptedBeanMetadataBean(beanManager));
        beanDeployer.addBuiltInBean(new DecoratedBeanMetadataBean(beanManager));
        beanDeployer.addBuiltInBean(new InterceptorMetadataBean(beanManager));
        beanDeployer.addBuiltInBean(new DecoratorMetadataBean(beanManager));
        if (Reflections.isClassLoadable(ServletApiAbstraction.SERVLET_CONTEXT_CLASS_NAME, WeldClassLoaderResourceLoader.INSTANCE)) {
            beanDeployer.addBuiltInBean(new HttpServletRequestBean(beanManager));
            beanDeployer.addBuiltInBean(new HttpSessionBean(beanManager));
            beanDeployer.addBuiltInBean(new ServletContextBean(beanManager));
        }
        if (beanManager.getServices().contains(TransactionServices.class)) {
            beanDeployer.addBuiltInBean(new UserTransactionBean(beanManager));
        }
        if (beanManager.getServices().contains(SecurityServices.class)) {
            beanDeployer.addBuiltInBean(new PrincipalBean(beanManager));
        }
        // Register the context beans
        for (ContextHolder<? extends Context> context : contexts) {
            beanDeployer.addBuiltInBean(ContextBean.of(context, beanManager));
        }

        if (beanDeploymentArchive.getBeansXml() instanceof TrimmableBeansXml) {
            TrimmableBeansXml beansXml = (TrimmableBeansXml) beanDeploymentArchive.getBeansXml();
            if (beansXml.isTrimmed()) {
                beanDeployer.getEnvironment().trim();
            }
        }
        // TODO Register the context beans
        beanDeployer.createClassBeans();

    }

    public void deploySpecialized(Environment environment) {
        beanDeployer.deploySpecialized();
    }

    // TODO Move class stuff into startContainer phase
    // TODO read EJB descriptors after reading classes
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
