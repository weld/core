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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.decorator.Decorator;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.enterprise.inject.spi.ProcessBeanAttributes;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.interceptor.Interceptor;

import org.jboss.weld.Container;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeContext;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeStore;
import org.jboss.weld.bean.AbstractBean;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.enablement.GlobalEnablementBuilder;
import org.jboss.weld.bootstrap.events.ProcessAnnotatedTypeImpl;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.spi.ClassFileServices;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.AnnotationApiAbstraction;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.collections.SetMultimap;

/**
 * @author Pete Muir
 * @author Jozef Hartinger
 * @author alesj
 * @author Marko Luksa
 */
public class BeanDeployer extends AbstractBeanDeployer<BeanDeployerEnvironment> {

    private final ResourceLoader resourceLoader;
    private final SlimAnnotatedTypeStore annotatedTypeStore;
    private final GlobalEnablementBuilder globalEnablementBuilder;
    private final AnnotationApiAbstraction annotationApi;
    private final ClassFileServices classFileServices;

    public BeanDeployer(BeanManagerImpl manager, ServiceRegistry services) {
        this(manager, services, BeanDeployerEnvironmentFactory.newEnvironment(manager));
    }

    public BeanDeployer(BeanManagerImpl manager, ServiceRegistry services, BeanDeployerEnvironment environment) {
        super(manager, services, environment);
        this.resourceLoader = manager.getServices().get(ResourceLoader.class);
        this.annotatedTypeStore = manager.getServices().get(SlimAnnotatedTypeStore.class);
        this.globalEnablementBuilder = manager.getServices().get(GlobalEnablementBuilder.class);
        this.annotationApi = manager.getServices().get(AnnotationApiAbstraction.class);
        this.classFileServices = manager.getServices().get(ClassFileServices.class);
    }

    /**
     * Loads a given class, creates a {@link SlimAnnotatedTypeContext} for it and stores it in {@link BeanDeployerEnvironment}.
     */
    public BeanDeployer addClass(String className, AnnotatedTypeLoader loader) {
        addIfNotNull(loader.loadAnnotatedType(className, getManager().getId()));
        return this;
    }

    public BeanDeployer addClass(Class<?> clazz, AnnotatedTypeLoader loader) {
        addIfNotNull(loader.loadAnnotatedType(clazz, getManager().getId()));
        return this;
    }

    private <T> SlimAnnotatedTypeContext<T> addIfNotNull(SlimAnnotatedTypeContext<T> ctx) {
        if (ctx != null) {
            getEnvironment().addAnnotatedType(ctx);
        }
        return ctx;
    }

    private void processPriority(AnnotatedType<?> type) {
        Object priority = type.getAnnotation(annotationApi.PRIORITY_ANNOTATION_CLASS);
        if (priority != null) {
            Integer value = annotationApi.getPriority(priority);
            if (value != null) {
                if (type.isAnnotationPresent(Interceptor.class)) {
                    globalEnablementBuilder.addInterceptor(type.getJavaClass(), value);
                } else if (type.isAnnotationPresent(Decorator.class)) {
                    globalEnablementBuilder.addDecorator(type.getJavaClass(), value);
                } else {
                    /*
                     * An alternative may be given a priority for the application by placing the @Priority annotation on the bean
                     * class that declares the producer method, field or resource.
                     */
                    globalEnablementBuilder.addAlternative(type.getJavaClass(), value);
                }
            }
        }
    }

    public <T> BeanDeployer addSyntheticClass(AnnotatedType<T> source, Extension extension, String suffix) {
        if (suffix == null) {
            suffix = AnnotatedTypes.createTypeId(source);
        }
        getEnvironment().addSyntheticAnnotatedType(classTransformer.getUnbackedAnnotatedType(source, getManager().getId(), suffix), extension);
        return this;
    }

    public BeanDeployer addClasses(Iterable<String> classes) {
        AnnotatedTypeLoader loader = createAnnotatedTypeLoader();
        for (String className : classes) {
            addClass(className, loader);
        }
        return this;
    }

    public BeanDeployer addLoadedClasses(Iterable<Class<?>> classes) {
        AnnotatedTypeLoader loader = createAnnotatedTypeLoader();
        for (Class<?> clazz : classes) {
            addClass(clazz, loader);
        }
        return this;
    }

    protected AnnotatedTypeLoader createAnnotatedTypeLoader() {
        if (classFileServices != null) {
            // Since FastProcessAnnotatedTypeResolver is installed after BeanDeployers are created, we need to query deploymentManager's services instead of the manager of this deployer
            final FastProcessAnnotatedTypeResolver resolver = Container.instance(getManager()).deploymentManager().getServices()
                    .get(FastProcessAnnotatedTypeResolver.class);
            if (resolver != null) {
                return new FastAnnotatedTypeLoader(getManager(), classTransformer, classFileServices, containerLifecycleEvents, resolver);
            }
        }
        // if FastProcessAnnotatedTypeResolver is not available, fall back to AnnotatedTypeLoader
        return new AnnotatedTypeLoader(getManager(), classTransformer, containerLifecycleEvents);
    }

    public void processAnnotatedTypes() {
        Set<SlimAnnotatedTypeContext<?>> classesToBeAdded = new HashSet<SlimAnnotatedTypeContext<?>>();
        Set<SlimAnnotatedTypeContext<?>> classesToBeRemoved = new HashSet<SlimAnnotatedTypeContext<?>>();

        for (SlimAnnotatedTypeContext<?> annotatedTypeContext : getEnvironment().getAnnotatedTypes()) {
            SlimAnnotatedType<?> annotatedType = annotatedTypeContext.getAnnotatedType();
            final ProcessAnnotatedTypeImpl<?> event = containerLifecycleEvents.fireProcessAnnotatedType(getManager(), annotatedTypeContext);

            // process the result
            if (event != null) {
                if (event.isVeto()) {
                    getEnvironment().vetoJavaClass(annotatedType.getJavaClass());
                    classesToBeRemoved.add(annotatedTypeContext);
                } else {
                    boolean dirty = event.isDirty();
                    if (dirty) {
                        classesToBeRemoved.add(annotatedTypeContext); // remove the original class
                        classesToBeAdded.add(SlimAnnotatedTypeContext.of(event.getResultingAnnotatedType(), annotatedTypeContext.getExtension()));
                    }
                    processPriority(event.getResultingAnnotatedType());
                }
            } else {
                processPriority(annotatedType);
            }
        }
        getEnvironment().removeAnnotatedTypes(classesToBeRemoved);
        getEnvironment().addAnnotatedTypes(classesToBeAdded);
    }

    public void registerAnnotatedTypes() {
        for (SlimAnnotatedTypeContext<?> ctx : getEnvironment().getAnnotatedTypes()) {
            annotatedTypeStore.put(ctx.getAnnotatedType());
        }
    }

    public void createClassBeans() {
        SetMultimap<Class<?>, SlimAnnotatedType<?>> otherWeldClasses = SetMultimap.newSetMultimap();

        for (SlimAnnotatedTypeContext<?> ctx : getEnvironment().getAnnotatedTypes()) {
            createClassBean(ctx.getAnnotatedType(), otherWeldClasses);
        }
        // create session beans
        ejbSupport.createSessionBeans(getEnvironment(), otherWeldClasses, getManager());
    }

    protected void createClassBean(SlimAnnotatedType<?> annotatedType, SetMultimap<Class<?>, SlimAnnotatedType<?>> otherWeldClasses) {
        boolean managedBeanOrDecorator = !ejbSupport.isEjb(annotatedType.getJavaClass()) && Beans.isTypeManagedBeanOrDecoratorOrInterceptor(annotatedType);
        if (managedBeanOrDecorator) {
            containerLifecycleEvents.preloadProcessInjectionTarget(annotatedType.getJavaClass());
            containerLifecycleEvents.preloadProcessBeanAttributes(annotatedType.getJavaClass());
            EnhancedAnnotatedType<?> weldClass = classTransformer.getEnhancedAnnotatedType(annotatedType);
            if (weldClass.isAnnotationPresent(Decorator.class)) {
                containerLifecycleEvents.preloadProcessBean(ProcessBean.class, annotatedType.getJavaClass());
                validateDecorator(weldClass);
                createDecorator(weldClass);
            } else if (weldClass.isAnnotationPresent(Interceptor.class)) {
                containerLifecycleEvents.preloadProcessBean(ProcessBean.class, annotatedType.getJavaClass());
                validateInterceptor(weldClass);
                createInterceptor(weldClass);
            } else if (!weldClass.isAbstract()) {
                containerLifecycleEvents.preloadProcessBean(ProcessManagedBean.class, annotatedType.getJavaClass());
                createManagedBean(weldClass);
            }
        } else {
            if (Beans.isDecoratorDeclaringInAppropriateConstructor(annotatedType)) {
                BootstrapLogger.LOG.decoratorWithNonCdiConstructor(annotatedType.getJavaClass().getName());
            }
            Class<? extends Annotation> scopeClass = Beans.getBeanDefiningAnnotationScope(annotatedType);
            if (scopeClass != null && !Beans.hasSimpleCdiConstructor(annotatedType)) {
                BootstrapLogger.LOG.annotatedTypeNotRegisteredAsBeanDueToMissingAppropriateConstructor(annotatedType.getJavaClass().getName(),
                        scopeClass.getSimpleName());
            }
            otherWeldClasses.put(annotatedType.getJavaClass(), annotatedType);
        }
    }

    /**
     * Fires {@link ProcessBeanAttributes} for each enabled bean and updates the environment based on the events.
     */
    public void processClassBeanAttributes() {
        preInitializeBeans(getEnvironment().getClassBeans());
        preInitializeBeans(getEnvironment().getDecorators());
        preInitializeBeans(getEnvironment().getInterceptors());

        processBeans(getEnvironment().getClassBeans());
        processBeans(getEnvironment().getDecorators());
        processBeans(getEnvironment().getInterceptors());
    }

    private void preInitializeBeans(Iterable<? extends AbstractBean<?, ?>> beans) {
        for (AbstractBean<?, ?> bean : beans) {
            bean.preInitialize();
        }
    }

    protected void processBeans(Iterable<? extends AbstractBean<?, ?>> beans) {
        // firstly, we handle PIT and PP
        processInjectionTargetEvents(beans);
        processProducerEvents(beans);
        // now we move onto PBA
        processBeanAttributes(beans);
    }

    protected void processBeanAttributes(Iterable<? extends AbstractBean<?, ?>> beans) {
        if (!containerLifecycleEvents.isProcessBeanAttributesObserved()) {
            return;
        }
        if (!beans.iterator().hasNext()) {
            return; // exit recursion
        }

        Collection<AbstractBean<?, ?>> vetoedBeans = new HashSet<AbstractBean<?, ?>>();
        Collection<AbstractBean<?, ?>> previouslySpecializedBeans = new HashSet<AbstractBean<?, ?>>();
        for (AbstractBean<?, ?> bean : beans) {
            // fire ProcessBeanAttributes for class beans
            boolean vetoed = fireProcessBeanAttributes(bean);
            if (vetoed) {
                vetoedBeans.add(bean);
            }
        }

        // remove vetoed class beans
        for (AbstractBean<?, ?> bean : vetoedBeans) {
            if (bean.isSpecializing()) {
                previouslySpecializedBeans.addAll(specializationAndEnablementRegistry.resolveSpecializedBeans(bean));
                specializationAndEnablementRegistry.vetoSpecializingBean(bean);
            }
            getEnvironment().vetoBean(bean);
        }
        // if a specializing bean was vetoed, let's process the specializing bean now
        processBeans(previouslySpecializedBeans);
    }

    public void createProducersAndObservers() {
        for (AbstractClassBean<?> bean : getEnvironment().getClassBeans()) {
            createObserversProducersDisposers(bean);
        }
    }

    public void processProducerAttributes() {
        processBeans(getEnvironment().getProducerFields());
        // process BeanAttributes for producer methods
        preInitializeBeans(getEnvironment().getProducerMethodBeans());
        processBeans(getEnvironment().getProducerMethodBeans());
    }

    public void deploy() {
        initializeBeans();
        fireProcessBeanEvents();
        deployBeans();
        initializeObserverMethods();
        deployObserverMethods();
    }

    protected void validateInterceptor(EnhancedAnnotatedType<?> weldClass) {
        if (weldClass.isAnnotationPresent(Decorator.class)) {
            throw BootstrapLogger.LOG.beanIsBothInterceptorAndDecorator(weldClass.getName());
        }
    }

    protected void validateDecorator(EnhancedAnnotatedType<?> weldClass) {
        if (weldClass.isAnnotationPresent(Interceptor.class)) {
            throw BootstrapLogger.LOG.beanIsBothInterceptorAndDecorator(weldClass.getName());
        }
    }

    public void doAfterBeanDiscovery(List<? extends Bean<?>> beanList) {
        for (Bean<?> bean : beanList) {
            if (bean instanceof RIBean<?>) {
                ((RIBean<?>) bean).initializeAfterBeanDiscovery();
            }
        }
    }

    public void registerCdiInterceptorsForMessageDrivenBeans() {
        ejbSupport.registerCdiInterceptorsForMessageDrivenBeans(getEnvironment(), getManager());
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public void cleanup() {
        getEnvironment().cleanup();
    }
}
