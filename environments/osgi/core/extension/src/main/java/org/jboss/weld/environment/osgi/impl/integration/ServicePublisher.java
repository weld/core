/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.weld.environment.osgi.impl.integration;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.jboss.weld.environment.osgi.api.annotation.Property;
import org.jboss.weld.environment.osgi.api.annotation.Publish;
import org.jboss.weld.environment.osgi.impl.extension.beans.RegistrationsHolderImpl;
import org.jboss.weld.environment.osgi.impl.extension.service.CDIOSGiExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * This is a class scanner that auto-publishes OSGi services from @Publish annotated classes within a bean bundle.
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class ServicePublisher {

    private static Logger logger = LoggerFactory.getLogger(ServicePublisher.class);

    private final Collection<String> classes;
    private final Bundle bundle;
    private final Instance<Object> instance;
    private final Set<String> blackList;

    public ServicePublisher(Collection<String> classes, Bundle bundle, Instance<Object> instance, Set<String> blackList) {
        logger.debug("Create a new ServicePublisher for bundle {}", bundle.getSymbolicName());
        this.classes = classes;
        this.bundle = bundle;
        this.instance = instance;
        this.blackList = blackList;
    }

    public void registerAndLaunchComponents() {
        logger.info("Registering/Starting OSGi Service for bundle {}", bundle.getSymbolicName());

        if (!classes.isEmpty()) {
            ClassPool classPool = new ClassPool();
            try {
                classPool.appendClassPath(new ClassClassPath(bundle.loadClass(classes.iterator().next())));
            } catch (ClassNotFoundException e) {
                logger.warn("Bundle {} is inaccessible", bundle);
            }
            CtClass ctClass = null;
            Class<?> clazz;
            for (String className : classes) {
                logger.trace("Scanning class {}", className);

                try {
                    ctClass = classPool.get(className);
                    if (ctClass.getAnnotation(Publish.class) != null) {
                        logger.debug("Found a new auto-published service class {}", className);
                        clazz = bundle.loadClass(className);
                        Object service = null;
                        InstanceHolder instanceHolder = instance.select(InstanceHolder.class).get();
                        List<Annotation> qualifiers = getQualifiers(clazz);
                        try {
                            Instance instance = instanceHolder.select(clazz, qualifiers.toArray(new Annotation[qualifiers.size()]));
                            service = instance.get();
                            logger.trace("Service instance generated");
                        } catch (Throwable e) {
                            logger.error("Unable to instantiate the service for class {}, CDI return this error: {}", clazz, e);
                            throw new RuntimeException(e);
                        }
                        publish(clazz, service, qualifiers);
                    }
                } catch (NotFoundException e) {//inaccessible class
                    logger.warn("Class file {} is inaccessible", className);
                } catch (ClassNotFoundException e) {
                    logger.warn("Class file {} cannot be read/load", className);
                } finally {
                    if (ctClass != null) {
                        ctClass.detach();
                        ctClass = null;
                    }
                }
            }
        }

    }

    private void publish(Class<?> clazz, Object service, List<Annotation> qualifiers) {
        logger.debug("Publishing a new service implementation {}", clazz.getSimpleName());
        ServiceRegistration registration = null;
        Publish publish = clazz.getAnnotation(Publish.class);
        Class[] contracts = publish.contracts();
        Properties properties = getServiceProperties(qualifiers);
        if (publish.rank() != 0) {
            properties.setProperty("service.rank", publish.rank() + "");
        }
        if (contracts.length > 0) {// if there are contracts
            String[] names = new String[contracts.length];
            for (int i = 0; i < contracts.length; i++) {
                if (contracts[i].isAssignableFrom(clazz) && contracts[i].isInterface()) {
                    names[i] = contracts[i].getName();
                    logger.info("Registering OSGi service {} as {}", clazz.getName(), names[i]);
                } else {
                    RuntimeException e = new RuntimeException("Contract " + contracts[i] + " is not assignable from "
                            + clazz + ", or is not an interface. Unable to publish the service " + clazz);
                    logger.error(e.getMessage());
                    throw e;
                }
            }
            registration = bundle.getBundleContext().registerService(names, service, properties);
        } else {
            if (clazz.getInterfaces().length > 0) {
                List<Class> interfaces = new ArrayList<Class>();
                for (Class itf : clazz.getInterfaces()) {
                    if (!blackList.contains(itf.getName())) {
                        interfaces.add(itf);
                    }
                }
                contracts = interfaces.toArray(new Class[interfaces.size()]);
            }
            if (contracts.length > 0) {// if there are non-blacklisted interfaces
                String[] names = new String[contracts.length];
                for (int i = 0; i < contracts.length; i++) {
                    names[i] = contracts[i].getName();
                    logger.info("Registering OSGi service {} as {}", clazz.getName(), names[i]);
                }
                registration = bundle.getBundleContext().registerService(names, service, properties);
            } else {
                Class superClass = clazz.getClass().getSuperclass();
                if (superClass != null && superClass != Object.class) {// if there is a superclass
                    logger.info("Registering OSGi service {} as {}", clazz.getName(), superClass.getName());
                    registration = bundle.getBundleContext().registerService(superClass.getName(), service, properties);
                } else {// publish directly with the implementation type
                    logger.info("Registering OSGi service {} as {}", clazz.getName(), clazz.getName());
                    registration = bundle.getBundleContext().registerService(clazz.getName(), service, properties);
                }
            }
        }
        if (registration != null) {
            CDIOSGiExtension.currentBundle.set(bundle.getBundleId());
            instance.select(RegistrationsHolderImpl.class).get().addRegistration(registration);
        }
    }

    private static Properties getServiceProperties(List<Annotation> qualifiers) {
        Properties properties = new Properties();
        if (!qualifiers.isEmpty()) {
            for (Annotation qualifier : qualifiers) {
                if (qualifier.annotationType().equals(Properties.class)) {
                    for (Property property : ((org.jboss.weld.environment.osgi.api.annotation.Properties) qualifier).value()) {
                        properties.setProperty(property.name(), property.value());
                    }
                } else if (!qualifier.annotationType().equals(Default.class) && !qualifier.annotationType().equals(Any.class)) {
                    if (qualifier.annotationType().getDeclaredMethods().length > 0) {
                        for (Method m : qualifier.annotationType().getDeclaredMethods()) {
                            if (!m.isAnnotationPresent(Nonbinding.class)) {
                                try {
                                    String key = qualifier.annotationType().getSimpleName().toLowerCase()
                                            + "." + m.getName().toLowerCase();
                                    Object value = m.invoke(qualifier);
                                    if (value == null) {
                                        value = m.getDefaultValue();
                                        if (value == null) {
                                            value = "*";
                                        }
                                    }
                                    properties.setProperty(key, value.toString());
                                } catch (Throwable t) {// ignore
                                }
                            }
                        }
                    } else {
                        properties.setProperty(qualifier.annotationType().getSimpleName().toLowerCase(), "*");
                    }
                }
            }
        }
        return properties;
    }

    private static List<Annotation> getQualifiers(Class<?> clazz) {
        List<Annotation> qualifiers = new ArrayList<Annotation>();
        for (Annotation annotation : clazz.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                qualifiers.add(annotation);
            }
        }
        return qualifiers;
    }
}
