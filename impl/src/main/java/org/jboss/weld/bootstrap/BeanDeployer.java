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

import static org.jboss.weld.util.cache.LoadingCacheUtils.getCacheValue;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.decorator.Decorator;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.enterprise.inject.spi.ProcessManagedBean;
import javax.interceptor.Interceptor;

import org.jboss.weld.Container;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeContext;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeStore;
import org.jboss.weld.bean.AbstractBean;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.attributes.BeanAttributesFactory;
import org.jboss.weld.bean.interceptor.InterceptorBindingsAdapter;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.enablement.GlobalEnablementBuilder;
import org.jboss.weld.bootstrap.events.ProcessAnnotatedTypeImpl;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.injection.producer.InterceptionModelInitializer;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.spi.ClassFileServices;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.AnnotationApiAbstraction;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.collections.Multimaps;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.cache.LoadingCache;

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

    public BeanDeployer(BeanManagerImpl manager, EjbDescriptors ejbDescriptors, ServiceRegistry services) {
        this(manager, ejbDescriptors, services, BeanDeployerEnvironmentFactory.newEnvironment(ejbDescriptors, manager));
    }

    public BeanDeployer(BeanManagerImpl manager, EjbDescriptors ejbDescriptors, ServiceRegistry services, BeanDeployerEnvironment environment) {
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
        SlimAnnotatedTypeContext<?> ctx = loader.loadAnnotatedType(className, getManager().getId());
        if (ctx != null) {
            getEnvironment().addAnnotatedType(ctx);
        }
        return this;
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

    protected AnnotatedTypeLoader createAnnotatedTypeLoader() {
        if (classFileServices != null) {
            // Since FastProcessAnnotatedTypeResolver is installed after BeanDeployers are created, we need to query deploymentManager's services instead of the manager of this deployer
            final FastProcessAnnotatedTypeResolver resolver = Container.instance(getManager()).deploymentManager().getServices().get(FastProcessAnnotatedTypeResolver.class);
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
                        classesToBeAdded.add(SlimAnnotatedTypeContext.of(event.getResultingAnnotatedType(), classTransformer, annotatedTypeContext.getExtension()));
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
        LoadingCache<Class<?>, Set<SlimAnnotatedType<?>>> otherWeldClasses = Multimaps.newConcurrentSetMultimap();

        for (SlimAnnotatedTypeContext<?> ctx : getEnvironment().getAnnotatedTypes()) {
            createClassBean(ctx.getAnnotatedType(), otherWeldClasses);
        }
        // create session beans
        for (InternalEjbDescriptor<?> ejbDescriptor : getEnvironment().getEjbDescriptors()) {
            if (getEnvironment().isVetoed(ejbDescriptor.getBeanClass()) || Beans.isVetoed(ejbDescriptor.getBeanClass())) {
                continue;
            }
            if (ejbDescriptor.isSingleton() || ejbDescriptor.isStateful() || ejbDescriptor.isStateless()) {
                if (otherWeldClasses.getIfPresent(ejbDescriptor.getBeanClass()) != null) {
                    for (SlimAnnotatedType<?> annotatedType : getCacheValue(otherWeldClasses, ejbDescriptor.getBeanClass())) {
                        EnhancedAnnotatedType<?> weldClass = classTransformer.getEnhancedAnnotatedType(annotatedType);
                        createSessionBean(ejbDescriptor, Reflections.<EnhancedAnnotatedType> cast(weldClass));
                    }
                } else {
                    createSessionBean(ejbDescriptor);
                }
            }
        }
    }

    protected void createClassBean(SlimAnnotatedType<?> annotatedType,
            LoadingCache<Class<?>, Set<SlimAnnotatedType<?>>> otherWeldClasses) {
        boolean managedBeanOrDecorator = !getEnvironment().getEjbDescriptors().contains(annotatedType.getJavaClass()) && Beans.isTypeManagedBeanOrDecoratorOrInterceptor(annotatedType);
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
            getCacheValue(otherWeldClasses, annotatedType.getJavaClass()).add(annotatedType);
        }
    }

    /**
     * Fires {@link ProcessBeanAttributes} for each enabled bean and updates the environment based on the events.
     */
    public void processClassBeanAttributes() {
        preInitializeBeans(getEnvironment().getClassBeans());
        preInitializeBeans(getEnvironment().getDecorators());
        preInitializeBeans(getEnvironment().getInterceptors());

        processBeanAttributes(getEnvironment().getClassBeans());
        processBeanAttributes(getEnvironment().getDecorators());
        processBeanAttributes(getEnvironment().getInterceptors());

        // now that we know that the bean won't be vetoed, it's the right time to register @New injection points
        searchForNewBeanDeclarations(getEnvironment().getClassBeans());
        searchForNewBeanDeclarations(getEnvironment().getDecorators());
        searchForNewBeanDeclarations(getEnvironment().getInterceptors());
    }

    private void preInitializeBeans(Iterable<? extends AbstractBean<?, ?>> beans) {
        for (AbstractBean<?, ?> bean : beans) {
            bean.preInitialize();
        }
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
        processBeanAttributes(previouslySpecializedBeans);
    }

    protected void searchForNewBeanDeclarations(Iterable<? extends AbstractBean<?, ?>> beans) {
        for (AbstractBean<?, ?> bean : beans) {
            getEnvironment().addNewBeansFromInjectionPoints(bean);
        }
    }

    public void createProducersAndObservers() {
        for (AbstractClassBean<?> bean : getEnvironment().getClassBeans()) {
            createObserversProducersDisposers(bean);
        }
    }

    public void processProducerAttributes() {
        processBeanAttributes(getEnvironment().getProducerFields());
        searchForNewBeanDeclarations(getEnvironment().getProducerFields());
        // process BeanAttributes for producer methods
        preInitializeBeans(getEnvironment().getProducerMethodBeans());
        processBeanAttributes(getEnvironment().getProducerMethodBeans());
        searchForNewBeanDeclarations(getEnvironment().getProducerMethodBeans());
    }

    public void createNewBeans() {
        for (EnhancedAnnotatedType<?> clazz : getEnvironment().getNewManagedBeanClasses()) {
            createNewManagedBean(clazz);
        }
        for (Entry<InternalEjbDescriptor<?>, EnhancedAnnotatedType<?>> entry : getEnvironment().getNewSessionBeanDescriptorsFromInjectionPoint().entrySet()) {
            InternalEjbDescriptor<?> descriptor = entry.getKey();
            createNewSessionBean(descriptor, BeanAttributesFactory.forSessionBean(entry.getValue(), descriptor, getManager()), entry.getValue());
        }
    }

    public void deploy() {
        initializeBeans();
        fireBeanEvents();
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
        EjbServices ejbServices = getManager().getServices().get(EjbServices.class);
        for (InternalEjbDescriptor<?> descriptor : getEnvironment().getEjbDescriptors()) {
            if (descriptor.isMessageDriven()) {
                EnhancedAnnotatedType<?> type =  classTransformer.getEnhancedAnnotatedType(descriptor.getBeanClass(), getManager().getId());
                if (!getManager().getInterceptorModelRegistry().containsKey(type.slim())) {
                    InterceptionModelInitializer.of(getManager(), type, null).init();
                }
                InterceptionModel model = getManager().getInterceptorModelRegistry().get(type.slim());
                if (model != null) {
                    ejbServices.registerInterceptors(descriptor, new InterceptorBindingsAdapter(model));
                }
            }
        }
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public void cleanup() {
        getEnvironment().cleanup();
    }
}
