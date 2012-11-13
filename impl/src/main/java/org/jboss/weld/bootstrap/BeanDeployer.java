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

import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BootstrapMessage.BEAN_IS_BOTH_INTERCEPTOR_AND_DECORATOR;
import static org.jboss.weld.logging.messages.BootstrapMessage.IGNORING_CLASS_DUE_TO_LOADING_ERROR;
import static org.slf4j.ext.XLogger.Level.DEBUG;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeStore;
import org.jboss.weld.bean.AbstractBean;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.attributes.BeanAttributesFactory;
import org.jboss.weld.bean.interceptor.InterceptorBindingsAdapter;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.events.ProcessAnnotatedTypeImpl;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.enums.EnumInjectionTarget;
import org.jboss.weld.enums.EnumService;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.injection.producer.InterceptionModelInitializer;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.logging.Category;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.collections.Multimaps;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;
import org.slf4j.ext.XLogger;

/**
 * @author Pete Muir
 * @author Jozef Hartinger
 * @author alesj
 */
public class BeanDeployer extends AbstractBeanDeployer<BeanDeployerEnvironment> {

    private transient LocLogger log = loggerFactory().getLogger(Category.CLASS_LOADING);
    private transient XLogger xlog = loggerFactory().getXLogger(Category.CLASS_LOADING);

    private final ResourceLoader resourceLoader;


    public BeanDeployer(BeanManagerImpl manager, EjbDescriptors ejbDescriptors, ServiceRegistry services) {
        this(manager, ejbDescriptors, services, BeanDeployerEnvironmentFactory.newEnvironment(ejbDescriptors, manager));
    }

    public BeanDeployer(BeanManagerImpl manager, EjbDescriptors ejbDescriptors, ServiceRegistry services, BeanDeployerEnvironment environment) {
        super(manager, services, environment);
        this.resourceLoader = manager.getServices().get(ResourceLoader.class);
    }

    public BeanDeployer addClass(String className) {
        Class<?> clazz = null;
        try {
            clazz = resourceLoader.classForName(className);
        } catch (ResourceLoadingException e) {
            log.info(IGNORING_CLASS_DUE_TO_LOADING_ERROR, className);
            xlog.catching(DEBUG, e);
        }

        if (clazz != null && !clazz.isAnnotation() && !Beans.isVetoed(clazz)) {
            containerLifecycleEvents.preloadProcessAnnotatedType(clazz);
            SlimAnnotatedType<?> annotatedType = null;
            try {
                annotatedType = classTransformer.getAnnotatedType(clazz);
            } catch (ResourceLoadingException e) {
                log.info(IGNORING_CLASS_DUE_TO_LOADING_ERROR, className);
                xlog.catching(DEBUG, e);
            }
            if (annotatedType != null) {
                getEnvironment().addAnnotatedType(annotatedType);
            }
        }
        return this;
    }

    public <T> BeanDeployer addSyntheticClass(AnnotatedType<T> annotatedType, Extension extension) {
        getEnvironment().addSyntheticAnnotatedType(classTransformer.getAnnotatedType(annotatedType), extension);
        return this;
    }

    public BeanDeployer addClasses(Iterable<String> classes) {
        for (String className : classes) {
            addClass(className);
        }
        return this;
    }

    public void processAnnotatedTypes() {
        Set<SlimAnnotatedType<?>> classesToBeAdded = new HashSet<SlimAnnotatedType<?>>();
        Set<AnnotatedType<?>> classesToBeRemoved = new HashSet<AnnotatedType<?>>();

        for (SlimAnnotatedType<?> annotatedType : getEnvironment().getAnnotatedTypes()) {
            // fire event
            ProcessAnnotatedTypeImpl<?> event = containerLifecycleEvents.fireProcessAnnotatedType(getManager(), annotatedType, getEnvironment().getAnnotatedTypeSource(annotatedType));
            // process the result
            if (event != null) {
                if (event.isVeto()) {
                    getEnvironment().vetoJavaClass(annotatedType.getJavaClass());
                    classesToBeRemoved.add(annotatedType);
                } else {
                    boolean dirty = event.isDirty();
                    if (dirty) {
                        classesToBeRemoved.add(annotatedType); // remove the original class
                        classesToBeAdded.add(event.getAnnotatedType());
                    }
                }
            }
        }
        getEnvironment().removeAnnotatedTypes(classesToBeRemoved);
        getEnvironment().addAnnotatedTypes(classesToBeAdded);
    }

    public void registerAnnotatedTypes() {
        SlimAnnotatedTypeStore store = getManager().getServices().get(SlimAnnotatedTypeStore.class);
        for (SlimAnnotatedType<?> type : getEnvironment().getAnnotatedTypes()) {
            store.put(type);
        }
    }

    public void processEnums() {
        EnumService enumService = getManager().getServices().get(EnumService.class);
        for (AnnotatedType<?> annotatedType: getEnvironment().getAnnotatedTypes()) {
            if (Reflections.isEnum(annotatedType.getJavaClass())) {
                enumService.addEnumClass(Reflections.<AnnotatedType<Enum<?>>> cast(annotatedType));
            }
        }
        // add @New injection points from enums
        for (EnumInjectionTarget<?> enumInjectionTarget : enumService.getEnumInjectionTargets()) {
            getEnvironment().addNewBeansFromInjectionPoints(enumInjectionTarget.getInjectionPoints());
        }
    }

    public void createClassBeans() {
        Map<Class<?>, Set<AnnotatedType<?>>> otherWeldClasses = Multimaps.newConcurrentSetMultimap();

        for (AnnotatedType<?> annotatedType : getEnvironment().getAnnotatedTypes()) {
            createClassBean(annotatedType, otherWeldClasses);
        }
        // create session beans
        for (InternalEjbDescriptor<?> ejbDescriptor : getEnvironment().getEjbDescriptors()) {
            if (getEnvironment().isVetoed(ejbDescriptor.getBeanClass()) || Beans.isVetoed(ejbDescriptor.getBeanClass())) {
                continue;
            }
            if (ejbDescriptor.isSingleton() || ejbDescriptor.isStateful() || ejbDescriptor.isStateless()) {
                if (otherWeldClasses.containsKey(ejbDescriptor.getBeanClass())) {
                    for (AnnotatedType<?> annotatedType : otherWeldClasses.get(ejbDescriptor.getBeanClass())) {
                        EnhancedAnnotatedType<?> weldClass = classTransformer.getEnhancedAnnotatedType(annotatedType);
                        createSessionBean(ejbDescriptor, Reflections.<EnhancedAnnotatedType> cast(weldClass));
                    }
                } else {
                    createSessionBean(ejbDescriptor);
                }
            }
        }
    }

    protected void createClassBean(AnnotatedType<?> annotatedType, Map<Class<?>, Set<AnnotatedType<?>>> otherWeldClasses) {
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
            otherWeldClasses.get(annotatedType.getJavaClass()).add(annotatedType);
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
                SpecializationAndEnablementRegistry registry = getManager().getServices().get(SpecializationAndEnablementRegistry.class);
                previouslySpecializedBeans.addAll(registry.resolveSpecializedBeans(bean));
                registry.vetoSpecializingBean(bean);
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
            throw new DeploymentException(BEAN_IS_BOTH_INTERCEPTOR_AND_DECORATOR, weldClass.getName());
        }
    }

    protected void validateDecorator(EnhancedAnnotatedType<?> weldClass) {
        if (weldClass.isAnnotationPresent(Interceptor.class)) {
            throw new DefinitionException(BEAN_IS_BOTH_INTERCEPTOR_AND_DECORATOR, weldClass.getName());
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
                if (!getManager().getInterceptorModelRegistry().containsKey(descriptor.getBeanClass())) {
                    InterceptionModelInitializer.of(getManager(), classTransformer.getEnhancedAnnotatedType(descriptor.getBeanClass()), null).init();
                }
                InterceptionModel<ClassMetadata<?>, ?> model = getManager().getInterceptorModelRegistry().get(descriptor.getBeanClass());
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
