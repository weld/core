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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.events.ProcessAnnotatedTypeFactory;
import org.jboss.weld.bootstrap.events.ProcessAnnotatedTypeImpl;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.introspector.DiscoveredExternalAnnotatedType;
import org.jboss.weld.introspector.ExternalAnnotatedType;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.logging.Category;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;
import org.slf4j.ext.XLogger;

import javax.decorator.Decorator;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.interceptor.Interceptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BootstrapMessage.BEAN_IS_BOTH_INTERCEPTOR_AND_DECORATOR;
import static org.jboss.weld.logging.messages.BootstrapMessage.IGNORING_CLASS_DUE_TO_LOADING_ERROR;
import static org.slf4j.ext.XLogger.Level.INFO;

/**
 * @author pmuir
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

        if (clazz != null && !clazz.isAnnotation() && !clazz.isEnum()) {
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
                if (event.isDirty()) {
                    iterator.remove();
                    AnnotatedType<?> modifiedType;
                    if (synthetic) {
                        modifiedType = ExternalAnnotatedType.of(event.getAnnotatedType());
                    } else {
                        modifiedType = DiscoveredExternalAnnotatedType.of(event.getAnnotatedType());
                    }
                    WeldClass<?> modifiedClass = classTransformer.loadClass(modifiedType);
                    processedClasses.add(modifiedClass);
                }
            }
        }
        classes.addAll(processedClasses);
    }

    public BeanDeployer createBeans() {
        Multimap<Class<?>, WeldClass<?>> otherWeldClasses = HashMultimap.create();
        for (WeldClass<?> clazz : classes) {
            boolean managedBeanOrDecorator = !getEnvironment().getEjbDescriptors().contains(clazz.getJavaClass()) && isTypeManagedBeanOrDecoratorOrInterceptor(clazz);
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
        for (InternalEjbDescriptor<?> ejbDescriptor : getEnvironment().getEjbDescriptors()) {
            if (vetoedClasses.contains(ejbDescriptor.getBeanClass())) {
                continue;
            }
            if (ejbDescriptor.isSingleton() || ejbDescriptor.isStateful() || ejbDescriptor.isStateless()) {
                if (otherWeldClasses.containsKey(ejbDescriptor.getBeanClass())) {
                    for (WeldClass<?> c : otherWeldClasses.get(ejbDescriptor.getBeanClass())) {
                        createSessionBean(ejbDescriptor, Reflections.<WeldClass>cast(c));
                    }
                } else {
                    createSessionBean(ejbDescriptor);
                }
            }
        }

        // Now create the new beans
        for (WeldClass<?> clazz : getEnvironment().getNewManagedBeanClasses()) {
            createNewManagedBean(clazz);
        }
        for (InternalEjbDescriptor<?> descriptor : getEnvironment().getNewSessionBeanDescriptors()) {
            createNewSessionBean(descriptor);
        }

        cleanup();

        return this;
    }

    private void validateInterceptor(WeldClass<?> clazz) {
        if (clazz.isAnnotationPresent(Decorator.class)) {
            throw new DeploymentException(BEAN_IS_BOTH_INTERCEPTOR_AND_DECORATOR, clazz.getName());
        }
    }

    private void validateDecorator(WeldClass<?> clazz) {
        if (clazz.isAnnotationPresent(Interceptor.class)) {
            throw new DeploymentException(BEAN_IS_BOTH_INTERCEPTOR_AND_DECORATOR, clazz.getName());
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
