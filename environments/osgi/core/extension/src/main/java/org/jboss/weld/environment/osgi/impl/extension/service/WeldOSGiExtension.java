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
package org.jboss.weld.environment.osgi.impl.extension.service;

import org.jboss.weld.environment.osgi.api.Service;
import org.jboss.weld.environment.osgi.api.annotation.Filter;
import org.jboss.weld.environment.osgi.api.annotation.OSGiService;
import org.jboss.weld.environment.osgi.api.annotation.Required;
import org.jboss.weld.environment.osgi.impl.extension.beans.OSGiUtilitiesProducer;
import org.jboss.weld.environment.osgi.impl.extension.OSGiServiceAnnotatedType;
import org.jboss.weld.environment.osgi.impl.extension.ExtensionActivator;
import org.jboss.weld.environment.osgi.impl.extension.FilterGenerator;
import org.jboss.weld.environment.osgi.impl.extension.OSGiServiceBean;
import org.jboss.weld.environment.osgi.impl.extension.OSGiServiceProducerBean;
import org.jboss.weld.environment.osgi.impl.extension.beans.BundleHolder;
import org.jboss.weld.environment.osgi.impl.extension.beans.ContainerObserver;
import org.jboss.weld.environment.osgi.impl.extension.beans.RegistrationsHolderImpl;
import org.jboss.weld.environment.osgi.impl.extension.beans.ServiceRegistryImpl;
import org.jboss.weld.environment.osgi.impl.integration.InstanceHolder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.enterprise.inject.spi.ProcessProducer;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.weld.environment.osgi.impl.Activator;

/**
 * Weld OSGi {@link  Extension}. Contains copy/paste parts from the GlassFish
 * OSGI-CDI extension.
 * <p/>
 * It registers utility beans for Weld-OSGi framework, process OSGi service
 * injection points and registers OSGi specific observers.
 * <b/>
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
@ApplicationScoped
public class WeldOSGiExtension implements Extension {

    private static Logger logger = LoggerFactory.getLogger(
            WeldOSGiExtension.class);
    private static boolean autoRunInHybridMode = true;
    // hack for weld integration
    public static ThreadLocal<Long> currentBundle = new ThreadLocal<Long>();
    private HashMap<Type, Set<InjectionPoint>> servicesToBeInjected =
            new HashMap<Type, Set<InjectionPoint>>();
    private HashMap<Type, Set<InjectionPoint>> serviceProducerToBeInjected =
            new HashMap<Type, Set<InjectionPoint>>();
    private List<Annotation> observers = new ArrayList<Annotation>();
    private Map<Class, Set<Filter>> requiredOsgiServiceDependencies =
            new HashMap<Class, Set<Filter>>();
    private ExtensionActivator activator;
    private List<Exception> exceptions = new ArrayList<Exception>();

    public void registerCDIOSGiBeans(@Observes BeforeBeanDiscovery event,
            BeanManager manager) {
        logger.debug("Observe a BeforeBeanDiscovery event");
        event.addAnnotatedType(
                manager.createAnnotatedType(OSGiUtilitiesProducer.class));
        event.addAnnotatedType(
                manager.createAnnotatedType(BundleHolder.class));
        event.addAnnotatedType(
                manager.createAnnotatedType(RegistrationsHolderImpl.class));
        event.addAnnotatedType(
                manager.createAnnotatedType(ServiceRegistryImpl.class));
        event.addAnnotatedType(
                manager.createAnnotatedType(ContainerObserver.class));
        event.addAnnotatedType(
                manager.createAnnotatedType(InstanceHolder.class));
    }

    /**
     * Processes all bean class for the current bundle in order to wrap them.
     * <p/>
     * @param event the bean class to be processed event.
     *
     * @see OSGiServiceAnnotatedType
     */
    public void discoverCDIOSGiClass(@Observes ProcessAnnotatedType<?> event) {
        logger.debug("Observe a ProcessAnnotatedType event");
        AnnotatedType annotatedType = event.getAnnotatedType();
        annotatedType = discoverAndProcessCDIOSGiClass(annotatedType);
        if (annotatedType != null) {
            event.setAnnotatedType(annotatedType);
        } else {
            logger.warn("The annotated type {} is ignored", annotatedType);
            event.veto();
        }
    }

    /**
     * Processes all OSGi service injection points ({@link OSGiService}
     * annotated, {@link Service} typed and {@link Required}).
     * <p/>
     * @param event the injection point to be processed event.
     */
    public void discoverCDIOSGiServices(@Observes ProcessInjectionTarget<?> event) {
        logger.debug("Observe a ProcessInjectionTarget event");
        Set<InjectionPoint> injectionPoints = event.getInjectionTarget().getInjectionPoints();
        discoverServiceInjectionPoints(injectionPoints);
    }

    public void afterProcessProducer(@Observes ProcessProducer<?, ?> event) {
        //Only using ProcessInjectionTarget for now.
        //TODO do we need to scan these events
    }

    public void afterProcessBean(@Observes ProcessBean<?> event) {
        //ProcessInjectionTarget and ProcessProducer take care of all relevant injection points.
        //TODO verify that :)
    }

    public void registerObservers(@Observes ProcessObserverMethod<?, ?> event) {
        logger.debug("Observe a ProcessObserverMethod event");
        Set<Annotation> qualifiers = event.getObserverMethod().getObservedQualifiers();
        for (Annotation qualifier : qualifiers) {
            if (qualifier.annotationType().equals(Filter.class)) {
                observers.add(qualifier);
            }
        }
    }

    /**
     * Creates the beans that match the discovered OSGi service injection points
     * ({@link OSGiService} annotated and {@link Service} typed).
     * <p/>
     * @param event the AfterBeanDiscovery event.
     */
    public void registerCDIOSGiServices(@Observes AfterBeanDiscovery event) {
        logger.debug("Observe an AfterBeanDiscovery event");
        //runExtension();
        for (Exception exception : exceptions) {
            logger.error("Registering a Weld-OSGi deployment error {}", exception);
            event.addDefinitionError(exception);
        }
        for (Iterator<Type> iterator = this.servicesToBeInjected.keySet().iterator();
                iterator.hasNext();) {
            Type type = iterator.next();
            if (!(type instanceof Class)) {
                //TODO: need to handle Instance<Class>. This fails currently
                logger.error("Unknown type: {}", type);
                event.addDefinitionError(
                        new UnsupportedOperationException("Injection target type "
                        + type + "not supported"));
                break;
            }
            addService(event, this.servicesToBeInjected.get(type));
        }

        for (Iterator<Type> iterator =
                this.serviceProducerToBeInjected.keySet().iterator();
                iterator.hasNext();) {
            Type type = iterator.next();
            addServiceProducer(event, this.serviceProducerToBeInjected.get(type));
        }
        if (!Activator.osgiStarted() && WeldOSGiExtension.autoRunInHybridMode) {
            logger.warn("Starting Weld-OSGi extension in hybrid mode.");
            runExtensionInHybridMode(); // should work if the current classloader is based on osgi classloader
        }
    }

    private void runExtensionInHybridMode() {
        if (!servicesToBeInjected.isEmpty()) {
            Set<InjectionPoint> injections =
                    servicesToBeInjected.values().iterator().next();
            if (!injections.isEmpty()) {
                InjectionPoint ip = injections.iterator().next();
                Class annotatedElt = ip.getMember().getDeclaringClass();
                BundleContext bc = BundleReference.class.cast(annotatedElt.getClassLoader()).getBundle().getBundleContext();
                activator = runInHybridMode(bc);
            }
        } else if (!serviceProducerToBeInjected.isEmpty()) {
            Set<InjectionPoint> injections =
                    serviceProducerToBeInjected.values().iterator().next();
            if (!injections.isEmpty()) {
                InjectionPoint ip = injections.iterator().next();
                Class annotatedElt = ip.getMember().getDeclaringClass();
                BundleContext bc = BundleReference.class.cast(annotatedElt.getClassLoader()).getBundle().getBundleContext();
                activator = runInHybridMode(bc);
            }
        } else {
            BundleContext bc = BundleReference.class.cast(getClass().getClassLoader()).getBundle().getBundleContext();
            logger.warn("Starting the extension assuming the bundle is {}",
                    bc.getBundle().getSymbolicName());
            activator = runInHybridMode(bc);
        }
    }

    /**
     * Method used to bootstrap the the OSGi part of the extension in an hybrid environment.
     * Provided for server integration purposes.
     *
     * @param bc
     * @param activator
     */
    public static void runInHybridMode(BundleContext bc, ExtensionActivator activator) {
        try {
            activator.start(bc);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Method used to bootstrap the the OSGi part of the extension in an hybrid environment.
     * Provided for server integration purposes.
     *
     * @param bc
     * @param activator
     */
    public static ExtensionActivator runInHybridMode(BundleContext bc) {
        ExtensionActivator activator = new ExtensionActivator();
        try {
            activator.start(bc);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return activator;
    }

    private AnnotatedType discoverAndProcessCDIOSGiClass(
            AnnotatedType annotatedType) {
        try {
            return new OSGiServiceAnnotatedType(annotatedType);
        } catch (Exception e) {
            exceptions.add(e);
        }
        return null;
    }

    private void discoverServiceInjectionPoints(
            Set<InjectionPoint> injectionPoints) {
        for (Iterator<InjectionPoint> iterator = injectionPoints.iterator();
                iterator.hasNext();) {
            InjectionPoint injectionPoint = iterator.next();

            boolean service = false;
            try {
                if (((ParameterizedType) injectionPoint.getType()).getRawType().equals(Service.class)) {
                    service = true;
                }
            } catch (Exception e) {//Not a ParameterizedType, skip
            }

            if (service) {
                addServiceProducerInjectionInfo(injectionPoint);
            } else if (contains(injectionPoint.getQualifiers(), OSGiService.class)) {
                addServiceInjectionInfo(injectionPoint);
            }
            if (contains(injectionPoint.getQualifiers(), Required.class)) {
                Class key;
                if (service) {
                    key = (Class) ((ParameterizedType) injectionPoint.getType()).getActualTypeArguments()[0];
                } else {
                    key = (Class) injectionPoint.getType();
                }
                Filter value = FilterGenerator.makeFilter(injectionPoint);
                if (!requiredOsgiServiceDependencies.containsKey(key)) {
                    requiredOsgiServiceDependencies.put(key, new HashSet<Filter>());
                }
                requiredOsgiServiceDependencies.get(key).add(value);
            }
        }
    }

    private void addServiceInjectionInfo(InjectionPoint injectionPoint) {
        Type key = injectionPoint.getType();
        if (!servicesToBeInjected.containsKey(key)) {
            servicesToBeInjected.put(key, new HashSet<InjectionPoint>());
        }
        servicesToBeInjected.get(key).add(injectionPoint);
    }

    private void addServiceProducerInjectionInfo(InjectionPoint injectionPoint) {
        Type key = injectionPoint.getType();
        if (!serviceProducerToBeInjected.containsKey(key)) {
            serviceProducerToBeInjected.put(key, new HashSet<InjectionPoint>());
        }
        serviceProducerToBeInjected.get(key).add(injectionPoint);
    }

    private void addService(AfterBeanDiscovery event,
            final Set<InjectionPoint> injectionPoints) {
        Set<OSGiServiceBean> beans = new HashSet<OSGiServiceBean>();
        for (Iterator<InjectionPoint> iterator = injectionPoints.iterator();
                iterator.hasNext();) {
            final InjectionPoint injectionPoint = iterator.next();
            beans.add(new OSGiServiceBean(injectionPoint));
        }
        for (OSGiServiceBean bean : beans) {
            event.addBean(bean);
        }
    }

    private void addServiceProducer(AfterBeanDiscovery event,
            final Set<InjectionPoint> injectionPoints) {
        Set<OSGiServiceProducerBean> beans = new HashSet<OSGiServiceProducerBean>();
        for (Iterator<InjectionPoint> iterator = injectionPoints.iterator();
                iterator.hasNext();) {
            final InjectionPoint injectionPoint = iterator.next();
            beans.add(new OSGiServiceProducerBean(injectionPoint));
        }
        for (OSGiServiceProducerBean bean : beans) {
            event.addBean(bean);
        }
    }

    private boolean contains(Set<Annotation> qualifiers,
            Class<? extends Annotation> qualifier) {
        for (Iterator<Annotation> iterator = qualifiers.iterator();
                iterator.hasNext();) {
            if (iterator.next().annotationType().equals(qualifier)) {
                return true;
            }
        }
        return false;
    }

    public List<Annotation> getObservers() {
        return observers;
    }

    public Map<Class, Set<Filter>> getRequiredOsgiServiceDependencies() {
        return requiredOsgiServiceDependencies;
    }

    public static boolean autoRunInHybridMode() {
        return autoRunInHybridMode;
    }

    /**
     * Set to false if the server use the manual bootstrap in hybrid mode with runInHybridMode
     * so the extension won't try to do it by itself.
     *
     * @param autoRunInHybridMode
     */
    public static void autoRunInHybridMode(boolean autoRunInHybridMode) {
        WeldOSGiExtension.autoRunInHybridMode = autoRunInHybridMode;
    }
}
