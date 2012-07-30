/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import java.io.File;
import java.io.InputStream;
import java.util.Dictionary;
import javax.naming.NamingException;
import org.jboss.weld.environment.osgi.api.Service;
import org.jboss.weld.environment.osgi.api.annotation.Filter;
import org.jboss.weld.environment.osgi.api.annotation.OSGiService;
import org.jboss.weld.environment.osgi.api.annotation.Required;
import org.jboss.weld.environment.osgi.impl.extension.beans.OSGiUtilitiesProducer;
import org.jboss.weld.environment.osgi.impl.extension.OSGiServiceAnnotatedType;
import org.jboss.weld.environment.osgi.impl.extension.FilterGenerator;
import org.jboss.weld.environment.osgi.impl.extension.OSGiServiceBean;
import org.jboss.weld.environment.osgi.impl.extension.OSGiServiceProducerBean;
import org.jboss.weld.environment.osgi.impl.extension.beans.BundleHolder;
import org.jboss.weld.environment.osgi.impl.extension.beans.ContainerObserver;
import org.jboss.weld.environment.osgi.impl.extension.beans.RegistrationsHolderImpl;
import org.jboss.weld.environment.osgi.impl.extension.beans.ServiceRegistryImpl;
import org.jboss.weld.environment.osgi.impl.integration.InstanceHolder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.BundleReference;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceRegistration;
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
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.jboss.weld.environment.osgi.api.annotation.Publish;
import org.jboss.weld.environment.osgi.api.events.AbstractBundleEvent;
import org.jboss.weld.environment.osgi.api.events.AbstractServiceEvent;
import org.jboss.weld.environment.osgi.api.events.BundleEvents;
import org.jboss.weld.environment.osgi.api.events.ServiceEvents;
import org.jboss.weld.environment.osgi.impl.Activator;
import org.jboss.weld.environment.osgi.impl.annotation.BundleNameAnnotation;
import org.jboss.weld.environment.osgi.impl.annotation.BundleVersionAnnotation;
import org.jboss.weld.environment.osgi.impl.annotation.FilterAnnotation;
import org.jboss.weld.environment.osgi.impl.annotation.SpecificationAnnotation;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;

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
    public static ThreadLocal<BundleContext> currentContext = new ThreadLocal<BundleContext>();
    private HashMap<Type, Set<InjectionPoint>> servicesToBeInjected =
            new HashMap<Type, Set<InjectionPoint>>();
    private HashMap<Type, Set<InjectionPoint>> serviceProducerToBeInjected =
            new HashMap<Type, Set<InjectionPoint>>();
    private List<Annotation> observers = new ArrayList<Annotation>();
    private Map<Class, Set<Filter>> requiredOsgiServiceDependencies =
            new HashMap<Class, Set<Filter>>();
    private List<Exception> exceptions = new ArrayList<Exception>();
    private List<Class<?>> publishableClasses = new ArrayList<Class<?>>();
    private BeanManager beanManager;
    private HybridListener listener;
    private BundleContextDelegate delegate;

    void registerCDIOSGiBeans(@Observes BeforeBeanDiscovery event,
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
        this.beanManager = manager;
        if (!Activator.osgiStarted() && WeldOSGiExtension.autoRunInHybridMode) {
            delegate = new BundleContextDelegate();
            currentContext.set(delegate);
        }
    }

    /**
     * Processes all bean class for the current bundle in order to wrap them.
     * <p/>
     * @param event the bean class to be processed event.
     *
     * @see OSGiServiceAnnotatedType
     */
    void discoverCDIOSGiClass(@Observes ProcessAnnotatedType<?> event) {
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
    void discoverCDIOSGiServices(@Observes ProcessInjectionTarget<?> event) {
        logger.debug("Observe a ProcessInjectionTarget event");
        Set<InjectionPoint> injectionPoints = event.getInjectionTarget().getInjectionPoints();
        discoverServiceInjectionPoints(injectionPoints);
    }

    void afterProcessProducer(@Observes ProcessProducer<?, ?> event) {
        //Only using ProcessInjectionTarget for now.
        //TODO do we need to scan these events
    }

    void afterProcessBean(@Observes ProcessBean<?> event) {
        //ProcessInjectionTarget and ProcessProducer take care of all relevant injection points.
        //TODO verify that :)
        Class<?> clazz = event.getBean().getBeanClass();
        if (clazz.isAnnotationPresent(Publish.class)) {
            publishableClasses.add(clazz);
        }
    }

    void registerObservers(@Observes ProcessObserverMethod<?, ?> event) {
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
    void registerCDIOSGiServices(@Observes AfterBeanDiscovery event) {
        logger.debug("Observe an AfterBeanDiscovery event");
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
    }

    public void startHybridMode() {
        if (!Activator.osgiStarted() && WeldOSGiExtension.autoRunInHybridMode) {
            logger.warn("Starting Weld-OSGi extension in hybrid mode.");
            runExtensionInHybridMode();
        }
    }

    public void startHybridMode(BundleContext ctx) {
         if (!Activator.osgiStarted() && WeldOSGiExtension.autoRunInHybridMode) {
            logger.warn("Starting Weld-OSGi extension in hybrid mode.");
            listener = runInHybridMode(ctx);
         }
    }

    private void runExtensionInHybridMode() {
        BundleContext bc = null;
        try {
            Context ctx = new InitialContext();
            bc = (BundleContext) ctx.lookup("java:comp/BundleContext");
            logger.info("JNDI lookup succeed :-)");
        } catch (NamingException ex) {
            logger.warn("Cannot lookup JNDI BundleContext.");
        }
        if (bc == null) {
            try {
                if (!servicesToBeInjected.isEmpty()) {
                    Set<InjectionPoint> injections =
                            servicesToBeInjected.values().iterator().next();
                    if (!injections.isEmpty()) {
                        InjectionPoint ip = injections.iterator().next();
                        Class annotatedElt = ip.getMember().getDeclaringClass();
                        bc = BundleReference.class.cast(annotatedElt.getClassLoader()).getBundle().getBundleContext();
                        listener = runInHybridMode(bc);
                    }
                } else if (!serviceProducerToBeInjected.isEmpty()) {
                    Set<InjectionPoint> injections =
                            serviceProducerToBeInjected.values().iterator().next();
                    if (!injections.isEmpty()) {
                        InjectionPoint ip = injections.iterator().next();
                        Class annotatedElt = ip.getMember().getDeclaringClass();
                        bc = BundleReference.class.cast(annotatedElt.getClassLoader()).getBundle().getBundleContext();
                        listener = runInHybridMode(bc);
                    }
                } else {
                    bc = BundleReference.class.cast(getClass().getClassLoader()).getBundle().getBundleContext();
                    logger.warn("Starting the extension assuming the bundle is {}",
                            bc.getBundle().getSymbolicName());
                    listener = runInHybridMode(bc);
                }
            } catch (Exception e) {
                logger.error("Unable to start Weld-OSGi in Hybrid mode.");
            }
        } else {
            listener = runInHybridMode(bc);
        }
    }

    /**
     * Method used to bootstrap the the OSGi part of the extension in an hybrid environment.
     * Provided for server integration purposes.
     *
     * @param bc
     * @param activator
     */
    private HybridListener runInHybridMode(BundleContext bc) {
        HybridListener list = new HybridListener(bc, this);
        delegate.setContext(bc);
        bc.addBundleListener(list);
        bc.addServiceListener(list);
        currentContext.set(bc);
        currentBundle.set(bc.getBundle().getBundleId());
        return list;
    }

    public void removeListeners() {
        if (delegate != null && listener != null) {
            delegate.removeBundleListener(listener);
            delegate.removeServiceListener(listener);
        }
    }

    void afterDeployment(@Observes AfterDeploymentValidation event) {
        if (listener != null) {
            currentContext.remove();
            currentBundle.remove();
        }
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
            beans.add(new OSGiServiceBean(injectionPoint, currentContext.get()));
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
            beans.add(new OSGiServiceProducerBean(injectionPoint, currentContext.get()));
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

    public List<Class<?>> getPublishableClasses() {
        return publishableClasses;
    }

    public static void setAutoRunInHybridMode(boolean autoRunInHybridMode) {
        WeldOSGiExtension.autoRunInHybridMode = autoRunInHybridMode;
    }

    public static boolean isAutoRunInHybridMode() {
        return autoRunInHybridMode;
    }

    private static class HybridListener implements SynchronousBundleListener,
                                                    ServiceListener {

        private static Logger logger = LoggerFactory.getLogger(HybridListener.class);

        private final BundleContext context;

        private final WeldOSGiExtension extension;

        public HybridListener(BundleContext context, WeldOSGiExtension extension) {
            this.context = context;
            this.extension = extension;
        }

        @Override
        public void bundleChanged(BundleEvent event) {
            Bundle bundle = event.getBundle();
            AbstractBundleEvent bundleEvent = null;
            switch(event.getType()) {
                case BundleEvent.INSTALLED:
                    logger.debug("Receiving a new OSGi bundle event INSTALLED");
                    bundleEvent = new BundleEvents.BundleInstalled(bundle);
                    break;
                case BundleEvent.LAZY_ACTIVATION:
                    logger.debug("Receiving a new OSGi bundle event LAZY_ACTIVATION");
                    bundleEvent = new BundleEvents.BundleLazyActivation(bundle);
                    break;
                case BundleEvent.RESOLVED:
                    logger.debug("Receiving a new OSGi bundle event RESOLVED");
                    bundleEvent = new BundleEvents.BundleResolved(bundle);
                    break;
                case BundleEvent.STARTED:
                    logger.debug("Receiving a new OSGi bundle event STARTED");
                    bundleEvent = new BundleEvents.BundleStarted(bundle);
                    break;
                case BundleEvent.STARTING:
                    logger.debug("Receiving a new OSGi bundle event STARTING");
                    bundleEvent = new BundleEvents.BundleStarting(bundle);
                    break;
                case BundleEvent.STOPPED:
                    logger.debug("Receiving a new OSGi bundle event STOPPED");
                    bundleEvent = new BundleEvents.BundleStopped(bundle);
                    break;
                case BundleEvent.STOPPING:
                    logger.debug("Receiving a new OSGi bundle event STOPPING");
                    bundleEvent = new BundleEvents.BundleStopping(bundle);
                    break;
                case BundleEvent.UNINSTALLED:
                    logger.debug("Receiving a new OSGi bundle event UNINSTALLED");
                    bundleEvent = new BundleEvents.BundleUninstalled(bundle);
                    break;
                case BundleEvent.UNRESOLVED:
                    logger.debug("Receiving a new OSGi bundle event UNRESOLVED");
                    bundleEvent = new BundleEvents.BundleUnresolved(bundle);
                    break;
                case BundleEvent.UPDATED:
                    logger.debug("Receiving a new OSGi bundle event UPDATED");
                    bundleEvent = new BundleEvents.BundleUpdated(bundle);
                    break;
            }
            boolean set = WeldOSGiExtension.currentBundle.get() != null;
            WeldOSGiExtension.currentBundle.set(context.getBundle().getBundleId());
            try {
                //broadcast the OSGi event through CDI event system
                extension.beanManager.fireEvent(event);
            }
            catch(Throwable t) {
                t.printStackTrace();
            }
            if (bundleEvent != null) {
                //broadcast the corresponding Weld-OSGi event
                fireAllBundleEvent(bundleEvent);
            }
            if (!set) {
                WeldOSGiExtension.currentBundle.remove();
            }
        }

        @Override
        public void serviceChanged(ServiceEvent event) {
            ServiceReference ref = event.getServiceReference();
            AbstractServiceEvent serviceEvent = null;
            switch(event.getType()) {
                case ServiceEvent.MODIFIED:
                    logger.debug("Receiving a new OSGi service event MODIFIED");
                    serviceEvent = new ServiceEvents.ServiceChanged(ref, context);
                    break;
                case ServiceEvent.REGISTERED:
                    logger.debug("Receiving a new OSGi service event REGISTERED");
                    serviceEvent = new ServiceEvents.ServiceArrival(ref, context);
                    break;
                case ServiceEvent.UNREGISTERING:
                    logger.debug("Receiving a new OSGi service event UNREGISTERING");
                    serviceEvent = new ServiceEvents.ServiceDeparture(ref, context);
                    break;
            }
            boolean set = WeldOSGiExtension.currentBundle.get() != null;
            WeldOSGiExtension.currentBundle.set(context.getBundle().getBundleId());
            try {
                //broadcast the OSGi event through CDI event system
                extension.beanManager.fireEvent(event);
            }
            catch(Throwable t) {
                t.printStackTrace();
            }
            if (serviceEvent != null) {
                //broadcast the corresponding Weld-OSGi event
                fireAllServiceEvent(serviceEvent);
            }
            if (!set) {
                WeldOSGiExtension.currentBundle.remove();
            }
        }

        private void fireAllServiceEvent(AbstractServiceEvent event) {
            List<Class<?>> classes = event.getServiceClasses(getClass());
            Class eventClass = event.getClass();
            for (Class<?> clazz : classes) {
                try {
                    Annotation[] qualifs = filteredServicesQualifiers(event,
                          new SpecificationAnnotation(clazz));
                    extension.beanManager.fireEvent(event, qualifs);
                }
                catch(Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        private Annotation[] filteredServicesQualifiers(AbstractServiceEvent event,
                                                        SpecificationAnnotation specific) {
            Set<Annotation> eventQualifiers = new HashSet<Annotation>();
            eventQualifiers.add(specific);
            for (Annotation annotation : extension.getObservers()) {
                String value = ((Filter) annotation).value();
                try {
                    org.osgi.framework.Filter filter = context.createFilter(value);
                    if (filter.match(event.getReference())) {
                        eventQualifiers.add(new FilterAnnotation(value));
                    }
                }
                catch(InvalidSyntaxException ex) {
                    //ex.printStackTrace();
                }
            }
            return eventQualifiers.toArray(new Annotation[eventQualifiers.size()]);
        }

        private void fireAllBundleEvent(AbstractBundleEvent event) {
            try {
                extension.beanManager.fireEvent(event,
                    new BundleNameAnnotation(event.getSymbolicName()),
                    new BundleVersionAnnotation(event.getVersion().toString()));
            }
            catch(Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static class BundleContextDelegate implements BundleContext {

        private BundleContext context;

        public void setContext(BundleContext context) {
            System.out.println("setup delegate context : " + context);
            this.context = context;
        }

        @Override
        public String toString() {
            return context.toString();
        }

        @Override
        public int hashCode() {
            return context.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return context.equals(obj);
        }

        @Override
        public boolean ungetService(ServiceReference reference) {
            return context.ungetService(reference);
        }

        @Override
        public void removeServiceListener(ServiceListener listener) {
            context.removeServiceListener(listener);
        }

        @Override
        public void removeFrameworkListener(FrameworkListener listener) {
            context.removeFrameworkListener(listener);
        }

        @Override
        public void removeBundleListener(BundleListener listener) {
            context.removeBundleListener(listener);
        }

        @Override
        public ServiceRegistration registerService(String clazz, Object service, Dictionary properties) {
            return context.registerService(clazz, service, properties);
        }

        @Override
        public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
            return context.registerService(clazzes, service, properties);
        }

        @Override
        public Bundle installBundle(String location) throws BundleException {
            return context.installBundle(location);
        }

        @Override
        public Bundle installBundle(String location, InputStream input) throws BundleException {
            return context.installBundle(location, input);
        }

        @Override
        public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
            return context.getServiceReferences(clazz, filter);
        }

        @Override
        public ServiceReference getServiceReference(String clazz) {
            return context.getServiceReference(clazz);
        }

        @Override
        public Object getService(ServiceReference reference) {
            return context.getService(reference);
        }

        @Override
        public String getProperty(String key) {
            return context.getProperty(key);
        }

        @Override
        public File getDataFile(String filename) {
            return context.getDataFile(filename);
        }

        @Override
        public Bundle[] getBundles() {
            return context.getBundles();
        }

        @Override
        public Bundle getBundle(long id) {
            return context.getBundle(id);
        }

        @Override
        public Bundle getBundle() {
            return context.getBundle();
        }

        @Override
        public ServiceReference[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
            return context.getAllServiceReferences(clazz, filter);
        }

        @Override
        public org.osgi.framework.Filter createFilter(String filter) throws InvalidSyntaxException {
            return context.createFilter(filter);
        }

        @Override
        public void addServiceListener(ServiceListener listener) {
            context.addServiceListener(listener);
        }

        @Override
        public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {
            context.addServiceListener(listener, filter);
        }

        @Override
        public void addFrameworkListener(FrameworkListener listener) {
            context.addFrameworkListener(listener);
        }

        @Override
        public void addBundleListener(BundleListener listener) {
            context.addBundleListener(listener);
        }
    }
}
