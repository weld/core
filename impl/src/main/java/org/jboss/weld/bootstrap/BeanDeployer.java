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
import static org.slf4j.ext.XLogger.Level.INFO;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.decorator.Decorator;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessManagedBean;
import javax.interceptor.Interceptor;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.bean.AbstractBean;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.attributes.BeanAttributesFactory;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.events.ProcessAnnotatedTypeFactory;
import org.jboss.weld.bootstrap.events.ProcessAnnotatedTypeImpl;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.enums.EnumInjectionTarget;
import org.jboss.weld.enums.EnumService;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.logging.Category;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.BeansClosure;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;
import org.slf4j.ext.XLogger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Pete Muir
 * @author Jozef Hartinger
 * @author alesj
 */
public class BeanDeployer extends AbstractBeanDeployer<BeanDeployerEnvironment> {

    private transient LocLogger log = loggerFactory().getLogger(Category.CLASS_LOADING);
    private transient XLogger xlog = loggerFactory().getXLogger(Category.CLASS_LOADING);

    private final ResourceLoader resourceLoader;
    protected final ClassTransformer classTransformer;

    public BeanDeployer(BeanManagerImpl manager, EjbDescriptors ejbDescriptors, ServiceRegistry services) {
        this(manager, ejbDescriptors, services, BeanDeployerEnvironment.newEnvironment(ejbDescriptors, manager));
    }

    public BeanDeployer(BeanManagerImpl manager, EjbDescriptors ejbDescriptors, ServiceRegistry services, BeanDeployerEnvironment environment) {
        super(manager, services, environment);
        this.resourceLoader = manager.getServices().get(ResourceLoader.class);
        this.classTransformer = manager.getServices().get(ClassTransformer.class);
    }

    public BeanDeployer addClass(String className) {
        Class<?> clazz = null;
        try {
            clazz = resourceLoader.classForName(className);
        } catch (ResourceLoadingException e) {
            log.info(IGNORING_CLASS_DUE_TO_LOADING_ERROR, className);
            xlog.catching(INFO, e);
        }

        if (clazz != null && !clazz.isAnnotation()) {
            preloadContainerLifecycleEvent(ProcessAnnotatedType.class, clazz);
            AnnotatedType<?> annotatedType = null;
            try {
                annotatedType = classTransformer.getAnnotatedType(clazz);
            } catch (ResourceLoadingException e) {
                log.info(IGNORING_CLASS_DUE_TO_LOADING_ERROR, className);
                xlog.catching(INFO, e);
            }
            if (annotatedType != null) {
                getEnvironment().addAnnotatedType(annotatedType);
            }
        }
        return this;
    }

    public <T> BeanDeployer addSyntheticClass(AnnotatedType<T> annotatedType, Extension extension) {
        SlimAnnotatedType<T> slim = ClassTransformer.instance(getManager()).getAnnotatedType(annotatedType);
        getEnvironment().addSyntheticAnnotatedType(slim, extension);
        return this;
    }

    public BeanDeployer addClasses(Iterable<String> classes) {
        for (String className : classes) {
            addClass(className);
        }
        return this;
    }

    public void processAnnotatedTypes() {
        Set<AnnotatedType<?>> classesToBeAdded = new HashSet<AnnotatedType<?>>();
        Set<AnnotatedType<?>> classesToBeRemoved = new HashSet<AnnotatedType<?>>();

        for (AnnotatedType<?> annotatedType : getEnvironment().getAnnotatedTypes()) {
            // fire event
            boolean synthetic = getEnvironment().getAnnotatedTypeSource(annotatedType) != null;
            ProcessAnnotatedTypeImpl<?> event;
            if (synthetic) {
                event = ProcessAnnotatedTypeFactory.create(getManager(), annotatedType, getEnvironment().getAnnotatedTypeSource(annotatedType));
            } else {
                event = ProcessAnnotatedTypeFactory.create(getManager(), annotatedType);
            }
            event.fire();
            // process the result
            if (event.isVeto()) {
                getEnvironment().vetoJavaClass(annotatedType.getJavaClass());
                classesToBeRemoved.add(annotatedType);
            } else {
                boolean dirty = event.isDirty();
                if (dirty) {
                    classesToBeRemoved.add(annotatedType); // remove the original class
                    AnnotatedType<?> modifiedType = event.getAnnotatedType();
                    if (modifiedType instanceof SlimAnnotatedType<?>) {
                        annotatedType = modifiedType;
                    } else {
                        annotatedType = classTransformer.getAnnotatedType(modifiedType);
                    }
                }

                // vetoed due to @Veto or @Requires
                boolean vetoed = Beans.isVetoed(annotatedType);

                if (dirty && !vetoed) {
                    classesToBeAdded.add(annotatedType); // add a replacement for the removed class
                }
                if (!dirty && vetoed) {
                    getEnvironment().vetoJavaClass(annotatedType.getJavaClass());
                    classesToBeRemoved.add(annotatedType);
                }
            }
        }
        getEnvironment().removeAnnotatedTypes(classesToBeRemoved);
        getEnvironment().addAnnotatedTypes(classesToBeAdded);
    }

    public void processEnums() {
        EnumService enumService = getManager().getServices().get(EnumService.class);
        for (AnnotatedType<?> annotatedType: getEnvironment().getAnnotatedTypes()) {
            if (Reflections.isEnum(annotatedType.getJavaClass())) {
                // TODO
                enumService.addEnumClass(Reflections.<AnnotatedType<Enum<?>>> cast(annotatedType));
            }
        }
        // add @New injection points from enums
        for (EnumInjectionTarget<?> enumInjectionTarget : enumService.getEnumInjectionTargets()) {
            getEnvironment().addNewBeansFromInjectionPoints(enumInjectionTarget.getNewInjectionPoints());
        }
    }

    public void createClassBeans() {
        Multimap<Class<?>, AnnotatedType<?>> otherWeldClasses = HashMultimap.create();

        for (AnnotatedType<?> annotatedType : getEnvironment().getAnnotatedTypes()) {
            createClassBean(annotatedType, otherWeldClasses);
        }
        // create session beans
        for (InternalEjbDescriptor<?> ejbDescriptor : getEnvironment().getEjbDescriptors()) {
            if (getEnvironment().isVetoed(ejbDescriptor.getBeanClass())) {
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

    protected void createClassBean(AnnotatedType<?> annotatedType, Multimap<Class<?>, AnnotatedType<?>> otherWeldClasses) {
        boolean managedBeanOrDecorator = !getEnvironment().getEjbDescriptors().contains(annotatedType.getJavaClass()) && Beans.isTypeManagedBeanOrDecoratorOrInterceptor(annotatedType);
        if (managedBeanOrDecorator) {
            preloadContainerLifecycleEvent(ProcessInjectionTarget.class, annotatedType.getJavaClass());
            preloadContainerLifecycleEvent(ProcessBeanAttributes.class, annotatedType.getJavaClass());
            EnhancedAnnotatedType<?> weldClass = classTransformer.getEnhancedAnnotatedType(annotatedType);
            if (weldClass.isAnnotationPresent(Decorator.class)) {
                preloadContainerLifecycleEvent(ProcessBean.class, annotatedType.getJavaClass());
                validateDecorator(weldClass);
                createDecorator(weldClass);
            } else if (weldClass.isAnnotationPresent(Interceptor.class)) {
                preloadContainerLifecycleEvent(ProcessBean.class, annotatedType.getJavaClass());
                validateInterceptor(weldClass);
                createInterceptor(weldClass);
            } else if (!weldClass.isAbstract()) {
                preloadContainerLifecycleEvent(ProcessManagedBean.class, annotatedType.getJavaClass());
                createManagedBean(weldClass);
            }
        } else {
            otherWeldClasses.put(annotatedType.getJavaClass(), annotatedType);
        }
    }

    /**
     * Fires {@link ProcessBeanAttributes} for each enabled bean and updates the environment based on the events.
     */
    public void processClassBeanAttributes() {
        preInitializeBeans(getEnvironment().getClassBeanMap().values());
        preInitializeBeans(getEnvironment().getDecorators());
        preInitializeBeans(getEnvironment().getInterceptors());

        processBeanAttributes(getEnvironment().getClassBeanMap().values());
        processBeanAttributes(getEnvironment().getDecorators());
        processBeanAttributes(getEnvironment().getInterceptors());
    }

    private void preInitializeBeans(Collection<? extends AbstractBean<?, ?>> beans) {
        for (AbstractBean<?, ?> bean : beans) {
            bean.preInitialize();
        }
    }

    protected void processBeanAttributes(Collection<? extends AbstractBean<?, ?>> beans) {
        if (beans.isEmpty()) {
            return; // exit recursion
        }

        Collection<AbstractBean<?, ?>> vetoedBeans = new HashSet<AbstractBean<?, ?>>();
        Collection<AbstractBean<?, ?>> previouslySpecializedBeans = new HashSet<AbstractBean<?, ?>>();
        for (AbstractBean<?, ?> bean : beans) {
            // fire ProcessBeanAttributes for class beans
            boolean vetoed = fireProcessBeanAttributes(bean);
            if (vetoed) {
                vetoedBeans.add(bean);
            } else {
                // now that we know that the bean won't be vetoed, it's the right time to register @New injection points
                getEnvironment().addNewBeansFromInjectionPoints(bean);
            }
        }

        // remove vetoed class beans
        for (AbstractBean<?, ?> bean : vetoedBeans) {
            if (bean.isSpecializing()) {
                BeansClosure.getClosure(getManager()).removeSpecialized(bean.getSpecializedBean());
                previouslySpecializedBeans.add(bean.getSpecializedBean());
            }
            getEnvironment().vetoBean(bean);
        }
        // if a specializing bean was vetoed, let's process the specializing bean now
        processBeanAttributes(previouslySpecializedBeans);
    }

    public void createProducersAndObservers() {
        for (AbstractClassBean<?> bean : getEnvironment().getClassBeanMap().values()) {
            createObserversProducersDisposers(bean);
        }
    }

    public void processProducerAttributes() {
        processBeanAttributes(getEnvironment().getProducerFields());
        // process BeanAttributes for producer methods
        preInitializeBeans(getEnvironment().getProducerMethodBeanMap().values());
        processBeanAttributes(getEnvironment().getProducerMethodBeanMap().values());
    }

    public void createNewBeans() {
        for (EnhancedAnnotatedType<?> clazz : getEnvironment().getNewManagedBeanClasses()) {
            createNewManagedBean(clazz);
        }
        for (Entry<InternalEjbDescriptor<?>, EnhancedAnnotatedType<?>> entry : getEnvironment().getNewSessionBeanDescriptorsFromInjectionPoint().entrySet()) {
            InternalEjbDescriptor<?> descriptor = entry.getKey();
            createNewSessionBean(descriptor, BeanAttributesFactory.forSessionBean(entry.getValue(), descriptor, getManager()));
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

    public void cleanup() {
        getEnvironment().cleanup();
    }
}
