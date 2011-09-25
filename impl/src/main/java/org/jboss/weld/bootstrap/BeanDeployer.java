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
import org.jboss.weld.bootstrap.events.ProcessAnnotatedTypeImpl;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.introspector.DiscoveredExternalAnnotatedType;
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
import javax.interceptor.Interceptor;
import java.util.HashSet;
import java.util.Set;

import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BootstrapMessage.BEAN_IS_BOTH_INTERCEPTOR_AND_DECORATOR;
import static org.jboss.weld.logging.messages.BootstrapMessage.IGNORING_CLASS_DUE_TO_LOADING_ERROR;
import static org.slf4j.ext.XLogger.Level.INFO;

/**
 * @author pmuir
 * @author alesj
 * @author Marko Luksa
 */
public class BeanDeployer extends AbstractBeanDeployer<BeanDeployerEnvironment> {

    private transient LocLogger log = loggerFactory().getLogger(Category.CLASS_LOADING);
    private transient XLogger xlog = loggerFactory().getXLogger(Category.CLASS_LOADING);

    private final Set<WeldClass<?>> classes;
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
        this.vetoedClasses = new HashSet<Class<?>>();
        this.resourceLoader = manager.getServices().get(ResourceLoader.class);
        this.classTransformer = Container.instance(manager.getContextId()).services().get(ClassTransformer.class);
    }

    public BeanDeployer addClass(String className) {
        Class<?> clazz = loadClass(className);
        if (clazz != null && isBeanCandidate(clazz)) {
            WeldClass<?> weldClass = loadWeldClass(clazz);
            if (weldClass != null) {
                ProcessAnnotatedTypeImpl<?> event = ProcessAnnotatedTypeImpl.fire(getManager(), weldClass);
                if (!event.isVeto()) {
                    if (event.getAnnotatedType() instanceof WeldClass<?>) {
                        classes.add((WeldClass<?>) event.getAnnotatedType());
                    } else {
                        classes.add(classTransformer.loadClass(DiscoveredExternalAnnotatedType.of(event.getAnnotatedType(), weldClass)));
                    }
                } else if (weldClass.isDiscovered()) {
                    vetoedClasses.add(weldClass.getJavaClass());
                }
            }
        }
        return this;
    }

    private boolean isBeanCandidate(Class<?> clazz) {
        try {
            return !clazz.isAnnotation() && !clazz.isEnum() && !Reflections.isNonStaticInnerClass(clazz);
        } catch (NoClassDefFoundError e) {
            logIgnoredClass(clazz.getName(), e);
            return false;
        }
    }

    private Class<?> loadClass(String className) {
        try {
            return resourceLoader.classForName(className);
        } catch (ResourceLoadingException e) {
            logIgnoredClass(className, e);
            return null;
        }
    }

    private WeldClass<?> loadWeldClass(Class<?> clazz) {
        try {
            return classTransformer.loadClass(clazz);
        } catch (ResourceLoadingException e) {
            logIgnoredClass(clazz.getName(), e);
            return null;
        }
    }

    private void logIgnoredClass(String className, Throwable e) {
        log.info(IGNORING_CLASS_DUE_TO_LOADING_ERROR, className);
        xlog.catching(INFO, e);
    }

    public BeanDeployer addClass(AnnotatedType<?> clazz) {
        classes.add(classTransformer.loadClass(clazz));
        return this;
    }

    public BeanDeployer addClasses(Iterable<String> classes) {
        for (String className : classes) {
            addClass(className);
        }
        return this;
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

}
