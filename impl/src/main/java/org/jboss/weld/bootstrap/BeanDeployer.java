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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.decorator.Decorator;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.interceptor.Interceptor;

import org.jboss.weld.Container;
import org.jboss.weld.bean.AbstractBean;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.ProducerMethod;
import org.jboss.weld.bean.attributes.BeanAttributesFactory;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment.WeldMethodKey;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.events.ProcessAnnotatedTypeFactory;
import org.jboss.weld.bootstrap.events.ProcessAnnotatedTypeImpl;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.enums.EnumInjectionTarget;
import org.jboss.weld.enums.EnumService;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.introspector.DiscoveredExternalAnnotatedType;
import org.jboss.weld.introspector.ExternalAnnotatedType;
import org.jboss.weld.introspector.WeldClass;
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
 */
public class BeanDeployer extends AbstractBeanDeployer<BeanDeployerEnvironment> {

    private transient LocLogger log = loggerFactory().getLogger(Category.CLASS_LOADING);
    private transient XLogger xlog = loggerFactory().getXLogger(Category.CLASS_LOADING);

    private final Set<WeldClass<?>> classes;
    private final Map<WeldClass<?>, Extension> classSource;
    private final Set<Class<?>> vetoedClasses;
    private final ResourceLoader resourceLoader;
    private final ClassTransformer classTransformer;

    /**
     * @param manager
     * @param ejbDescriptors
     */
    public BeanDeployer(BeanManagerImpl manager, EjbDescriptors ejbDescriptors, ServiceRegistry services) {
        super(manager, services, new BeanDeployerEnvironment(ejbDescriptors, manager));
        this.classes = new HashSet<WeldClass<?>>();
        this.classSource = new HashMap<WeldClass<?>, Extension>();
        this.vetoedClasses = new HashSet<Class<?>>();
        this.resourceLoader = manager.getServices().get(ResourceLoader.class);
        this.classTransformer = Container.instance().services().get(ClassTransformer.class);
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
            WeldClass<?> weldClass = null;
            try {
                weldClass = classTransformer.loadClass(clazz);
            } catch (ResourceLoadingException e) {
                log.info(IGNORING_CLASS_DUE_TO_LOADING_ERROR, className);
                xlog.catching(INFO, e);
            }
            if (weldClass != null) {
                classes.add(weldClass);
            }
        }
        return this;
    }

    public BeanDeployer addSyntheticClass(AnnotatedType<?> clazz, Extension extension) {
        WeldClass<?> weldClass = classTransformer.loadClass(clazz);
        classes.add(weldClass);
        classSource.put(weldClass, extension);
        return this;
    }

    public BeanDeployer addClasses(Iterable<String> classes) {
        for (String className : classes) {
            addClass(className);
        }
        return this;
    }

    public void processAnnotatedTypes() {
        Set<WeldClass<?>> processedClasses = new HashSet<WeldClass<?>>();
        for (Iterator<WeldClass<?>> iterator = classes.iterator(); iterator.hasNext();) {
            WeldClass<?> weldClass = iterator.next();
            // fire event
            boolean synthetic = classSource.containsKey(weldClass);
            ProcessAnnotatedTypeImpl<?> event;
            if (synthetic) {
                event = ProcessAnnotatedTypeFactory.create(getManager(), weldClass, classSource.get(weldClass));
            } else {
                event = ProcessAnnotatedTypeFactory.create(getManager(), weldClass);
            }
            event.fire();
            // process the result
            if (event.isVeto()) {
                iterator.remove();
                if (weldClass.isDiscovered()) {
                    vetoedClasses.add(weldClass.getJavaClass());
                }
            } else {
                boolean dirty = event.isDirty();
                if (dirty) {
                    iterator.remove();
                    AnnotatedType<?> modifiedType;
                    if (synthetic) {
                        modifiedType = ExternalAnnotatedType.of(event.getAnnotatedType());
                    } else {
                        modifiedType = DiscoveredExternalAnnotatedType.of(event.getAnnotatedType());
                    }
                    weldClass = classTransformer.loadClass(modifiedType);
                }

                // vetoed due to @Veto or @Requires
                boolean vetoed = Beans.isVetoed(weldClass);

                if (dirty && !vetoed) {
                    processedClasses.add(weldClass);
                }
                if (!dirty && vetoed) {
                    iterator.remove();
                }
            }
        }
        classes.addAll(processedClasses);
    }

    public void createClassBeans() {
        Multimap<Class<?>, WeldClass<?>> otherWeldClasses = HashMultimap.create();
        EnumService enumService = getManager().getServices().get(EnumService.class);

        for (WeldClass<?> clazz : classes) {
            if (Reflections.isEnum(clazz.getJavaClass())) {
                enumService.addEnumClass(Reflections.<WeldClass<Enum<?>>> cast(clazz));
            }
            boolean managedBeanOrDecorator = !getEnvironment().getEjbDescriptors().contains(clazz.getJavaClass()) && Beans.isTypeManagedBeanOrDecoratorOrInterceptor(clazz);
            if (managedBeanOrDecorator && clazz.isAnnotationPresent(Decorator.class)) {
                validateDecorator(clazz);
                createDecorator(clazz);
            } else if (managedBeanOrDecorator && clazz.isAnnotationPresent(Interceptor.class)) {
                validateInterceptor(clazz);
                createInterceptor(clazz);
            } else if (managedBeanOrDecorator && !clazz.isAbstract()) {
                createManagedBean(clazz);
            } else {
                otherWeldClasses.put(clazz.getJavaClass(), clazz);
            }
        }
        // add @New injection points from enums
        for (EnumInjectionTarget<?> enumInjectionTarget : enumService.getEnumInjectionTargets()) {
            getEnvironment().addNewBeansFromInjectionPoints(enumInjectionTarget.getNewInjectionPoints());
        }
        // create session beans
        for (InternalEjbDescriptor<?> ejbDescriptor : getEnvironment().getEjbDescriptors()) {
            if (vetoedClasses.contains(ejbDescriptor.getBeanClass())) {
                continue;
            }
            if (ejbDescriptor.isSingleton() || ejbDescriptor.isStateful() || ejbDescriptor.isStateless()) {
                if (otherWeldClasses.containsKey(ejbDescriptor.getBeanClass())) {
                    for (WeldClass<?> c : otherWeldClasses.get(ejbDescriptor.getBeanClass())) {
                        createSessionBean(ejbDescriptor, Reflections.<WeldClass> cast(c));
                    }
                } else {
                    createSessionBean(ejbDescriptor);
                }
            }
        }
    }

    /**
     * Fires {@link ProcessBeanAttributes} for each enabled bean and updates the environment based on the events.
     */
    public void processBeans() {
        for (Entry<WeldClass<?>, AbstractClassBean<?>> entry : getEnvironment().getClassBeanMap().entrySet()) {
            // process specialization
            entry.getValue().preInitialize();
        }

        processBeanAttributes(getEnvironment().getClassBeanMap().values());
    }

    private void processBeanAttributes(Collection<? extends AbstractBean<?, ?>> beans) {
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
            if (bean instanceof AbstractClassBean<?>) {
                getEnvironment().removeClass((WeldClass<?>) bean.getWeldAnnotated());
            }
            if (bean instanceof ProducerMethod<?, ?>) {
                getEnvironment().removeProducerMethod((ProducerMethod<?, ?>) bean);
            }
        }
        // if a specializing bean was vetoed, let's process the specializing bean now
        processBeanAttributes(previouslySpecializedBeans);
    }

    public void createProducersAndObservers() {
        for (AbstractClassBean<?> bean : getEnvironment().getClassBeanMap().values()) {
            createObserversProducersDisposers(bean);
        }
    }

    public void processProducerMethods() {
        // process BeanAttributes for producer methods
        for (Entry<WeldMethodKey<?, ?>, ProducerMethod<?, ?>> entry : getEnvironment().getProducerMethodBeanMap().entrySet()) {
            // process specialization
            entry.getValue().preInitialize();
        }
        processBeanAttributes(getEnvironment().getProducerMethodBeanMap().values());
    }

    public void createNewBeans() {
        for (WeldClass<?> clazz : getEnvironment().getNewManagedBeanClasses()) {
            createNewManagedBean(clazz);
        }
        for (Entry<InternalEjbDescriptor<?>, WeldClass<?>> entry : getEnvironment().getNewSessionBeanDescriptorsFromInjectionPoint().entrySet()) {
            InternalEjbDescriptor<?> descriptor = entry.getKey();
            createNewSessionBean(descriptor, BeanAttributesFactory.forSessionBean(entry.getValue(), descriptor, getManager()));
        }
    }

    private void validateInterceptor(WeldClass<?> weldClass) {
        if (weldClass.isAnnotationPresent(Decorator.class)) {
            throw new DeploymentException(BEAN_IS_BOTH_INTERCEPTOR_AND_DECORATOR, weldClass.getName());
        }
    }

    private void validateDecorator(WeldClass<?> weldClass) {
        if (weldClass.isAnnotationPresent(Interceptor.class)) {
            throw new DeploymentException(BEAN_IS_BOTH_INTERCEPTOR_AND_DECORATOR, weldClass.getName());
        }
    }

    public Set<WeldClass<?>> getClasses() {
        return Collections.unmodifiableSet(classes);
    }

    public void cleanup() {
        classes.clear();
        classSource.clear();
        vetoedClasses.clear();
    }
}
