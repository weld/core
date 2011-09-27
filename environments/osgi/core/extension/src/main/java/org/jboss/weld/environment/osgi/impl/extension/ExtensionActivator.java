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
package org.jboss.weld.environment.osgi.impl.extension;

import org.jboss.weld.environment.osgi.impl.annotation.FilterAnnotation;
import org.jboss.weld.environment.osgi.impl.annotation.SpecificationAnnotation;
import org.jboss.weld.environment.osgi.impl.annotation.BundleVersionAnnotation;
import org.jboss.weld.environment.osgi.impl.annotation.BundleNameAnnotation;
import org.jboss.weld.environment.osgi.api.annotation.Filter;
import org.jboss.weld.environment.osgi.api.events.AbstractBundleEvent;
import org.jboss.weld.environment.osgi.api.events.AbstractServiceEvent;
import org.jboss.weld.environment.osgi.api.events.BundleEvents;
import org.jboss.weld.environment.osgi.api.events.ServiceEvents;
import org.jboss.weld.environment.osgi.impl.Activator;
import org.jboss.weld.environment.osgi.impl.extension.service.WeldOSGiExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Extension;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is the activator of the Weld-OSGi extension part.
 * It starts with the extension originBundle.
 * <p/>
 * It seems we cannot get the {@link BundleContext} in the Extension,
 * so to fire up OSGi events ({@link BundleEvent}, {@link ServiceEvent})
 * we need to act here.
 * <p/>
 * It is responsible for broadcasting OSGi events through CDI events system.
 * It is a {@link SynchronousBundleListener}, so every event is synchronously
 * distached to CDI listeners (but not synchronously treated).
 * {@link SynchronousBundleListener} is needed in order to catch
 * {@link BundleEvent#STARTING} and {@link BundleEvent#STOPPING} events.
 * <p/>
 * @see Activator
 * @see Extension
 * @see WeldOSGiExtension
 *
 * @author Guillaume Sauthier
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class ExtensionActivator implements BundleActivator,
                                           SynchronousBundleListener,
                                           ServiceListener {

    private static Logger logger =
                          LoggerFactory.getLogger(ExtensionActivator.class);

    private BundleContext context;

    @Override
    public void start(BundleContext context) throws Exception {
        logger.trace("Entering ExtensionActivator : start() with parameter {}",
                     new Object[] {context});
        this.context = context;
        context.addBundleListener(this);
        context.addServiceListener(this);
        logger.debug("Weld-OSGi extension part STARTED");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.trace("Entering ExtensionActivator : stop() with parameter {}",
                     new Object[] {context});
        //nothing to do here
        logger.debug("Weld-OSGi extension part STOPPED");
    }

    /**
     * This method broadcast {@link BundleEvent} and a corresonding
     * {@link AbstractBundleEvent} from OSGi framework to any registered CDI
     * {@link Event} OSGi service. All managed bean originBundle  register
     * such a service during startup.
     * <p/>
     *
     * @param event the received {@link BundleEvent} to broadcast.
     */
    @Override
    public void bundleChanged(BundleEvent event) {
        logger.trace("Entering ExtensionActivator : "
                     + "bundleChanged() with parameter {}",
                     new Object[] {event});
        ServiceReference[] cdiEventServiceReferences =
                           findReferences(context, Event.class);
        if (cdiEventServiceReferences != null) {
            Bundle originBundle = event.getBundle();
            AbstractBundleEvent resultingWeldOSGiBundleEvent = null;
            switch(event.getType()) {
                case BundleEvent.INSTALLED:
                    logger.debug("Receiving a new OSGi bundle event INSTALLED");
                    resultingWeldOSGiBundleEvent =
                    new BundleEvents.BundleInstalled(originBundle);
                    break;
                case BundleEvent.LAZY_ACTIVATION:
                    logger.debug("Receiving a new OSGi bundle event LAZY_ACTIVATION");
                    resultingWeldOSGiBundleEvent =
                    new BundleEvents.BundleLazyActivation(originBundle);
                    break;
                case BundleEvent.RESOLVED:
                    logger.debug("Receiving a new OSGi bundle event RESOLVED");
                    resultingWeldOSGiBundleEvent =
                    new BundleEvents.BundleResolved(originBundle);
                    break;
                case BundleEvent.STARTED:
                    logger.debug("Receiving a new OSGi bundle event STARTED");
                    resultingWeldOSGiBundleEvent =
                    new BundleEvents.BundleStarted(originBundle);
                    break;
                case BundleEvent.STARTING:
                    logger.debug("Receiving a new OSGi bundle event STARTING");
                    resultingWeldOSGiBundleEvent =
                    new BundleEvents.BundleStarting(originBundle);
                    break;
                case BundleEvent.STOPPED:
                    logger.debug("Receiving a new OSGi bundle event STOPPED");
                    resultingWeldOSGiBundleEvent =
                    new BundleEvents.BundleStopped(originBundle);
                    break;
                case BundleEvent.STOPPING:
                    logger.debug("Receiving a new OSGi bundle event STOPPING");
                    resultingWeldOSGiBundleEvent =
                    new BundleEvents.BundleStopping(originBundle);
                    break;
                case BundleEvent.UNINSTALLED:
                    logger.debug("Receiving a new OSGi bundle event UNINSTALLED");
                    resultingWeldOSGiBundleEvent =
                    new BundleEvents.BundleUninstalled(originBundle);
                    break;
                case BundleEvent.UNRESOLVED:
                    logger.debug("Receiving a new OSGi bundle event UNRESOLVED");
                    resultingWeldOSGiBundleEvent =
                    new BundleEvents.BundleUnresolved(originBundle);
                    break;
                case BundleEvent.UPDATED:
                    logger.debug("Receiving a new OSGi bundle event UPDATED");
                    resultingWeldOSGiBundleEvent =
                    new BundleEvents.BundleUpdated(originBundle);
                    break;
            }
            for (ServiceReference reference : cdiEventServiceReferences) {
                boolean set = WeldOSGiExtension.currentBundle.get() != null;
                WeldOSGiExtension.currentBundle.set(reference.getBundle().getBundleId());
                Event<Object> broadcastingCDIEvent =
                              (Event<Object>) context.getService(reference);
                try {
                    broadcastingCDIEvent.select(BundleEvent.class).fire(event);
                }
                catch(Throwable t) {
                    //ignore
                }
                if (resultingWeldOSGiBundleEvent != null) {
                    fireAllEvent(resultingWeldOSGiBundleEvent, broadcastingCDIEvent);
                }
                if (!set) {
                    WeldOSGiExtension.currentBundle.remove();
                }
            }
        }
    }

    /**
     * This method broadcast {@link ServiceEvent} and a corresonding
     * {@link AbstractServiceEvent} from OSGi framework to any registered CDI
     * {@link Event} OSGi service. All managed bean originBundle  register
     * such a service during startup.
     * <p/>
     *
     * @param event the received {@link ServiceEvent} to broadcast.
     */
    @Override
    public void serviceChanged(ServiceEvent event) {
        logger.trace("Entering ExtensionActivator : "
                     + "serviceChanged() with parameter {}",
                     new Object[] {event});
        ServiceReference[] cdiInstanceServiceReferences =
                           findReferences(context, Instance.class);
        if (cdiInstanceServiceReferences != null) {
            ServiceReference originServiceReference = event.getServiceReference();
            AbstractServiceEvent resultingWeldOSGiServiceEvent = null;
            switch(event.getType()) {
                case ServiceEvent.MODIFIED:
                    logger.debug("Receiving a new OSGi service event MODIFIED");
                    resultingWeldOSGiServiceEvent =
                    new ServiceEvents.ServiceChanged(originServiceReference,
                                                     context);
                    break;
                case ServiceEvent.REGISTERED:
                    logger.debug("Receiving a new OSGi service event REGISTERED");
                    resultingWeldOSGiServiceEvent =
                    new ServiceEvents.ServiceArrival(originServiceReference,
                                                     context);
                    break;
                case ServiceEvent.UNREGISTERING:
                    logger.debug("Receiving a new OSGi service event UNREGISTERING");
                    resultingWeldOSGiServiceEvent =
                    new ServiceEvents.ServiceDeparture(originServiceReference,
                                                       context);
                    break;
            }
            for (ServiceReference reference : cdiInstanceServiceReferences) {
                boolean set = WeldOSGiExtension.currentBundle.get() != null;
                WeldOSGiExtension.currentBundle.set(reference.getBundle().getBundleId());
                Instance<Object> instance =
                                 (Instance<Object>) context.getService(reference);
                try {
                    Event<Object> broadcastingCDIEvent =
                                  instance.select(Event.class).get();
                    broadcastingCDIEvent.select(ServiceEvent.class).fire(event);
                    if (resultingWeldOSGiServiceEvent != null) {
                        fireAllEvent(resultingWeldOSGiServiceEvent,
                                     broadcastingCDIEvent,
                                     instance);
                    }
                }
                catch(Throwable t) {
                    //ignore
                }
                if (!set) {
                    WeldOSGiExtension.currentBundle.remove();
                }
            }
        }
    }

    private ServiceReference[] findReferences(BundleContext context,
                                              Class<?> type) {
        logger.trace("Entering ExtensionActivator : "
                     + "findReferences() with parameters {} | {}",
                     new Object[] {context,
                                   type});
        ServiceReference[] references = null;
        try {
            references = context.getServiceReferences(type.getName(), null);
        }
        catch(InvalidSyntaxException e) {
            //ignore
        }
        return references;
    }

    private void fireAllEvent(AbstractBundleEvent event, Event broadcaster) {
        logger.trace("Entering ExtensionActivator : "
                     + "fireAllEvent() with parameters {}",
                     new Object[] {event});
        try {
            broadcaster.select(event.getClass(),
                               new BundleNameAnnotation(event.getSymbolicName()),
                               new BundleVersionAnnotation(event.getVersion().toString())).fire(event);
        }
        catch(Throwable t) {
            //ignore
        }
    }

    private void fireAllEvent(AbstractServiceEvent event,
                              Event broadcaster,
                              Instance<Object> instance) {
        logger.trace("Entering ExtensionActivator : "
                     + "fireAllEvent() with parameters {} | {}",
                     new Object[] {event,
                                   instance});
        List<Class<?>> classes = event.getServiceClasses(getClass());
        Class eventClass = event.getClass();
        for (Class<?> clazz : classes) {
            try {
                // here singleton issue
                broadcaster.select(eventClass,
                                   filteredServicesQualifiers(event,
                                                              new SpecificationAnnotation(clazz),
                                                              instance)).fire(event);
            }
            catch(Throwable t) {
                //ignore
            }
        }
    }

    private Annotation[] filteredServicesQualifiers(AbstractServiceEvent event,
                                                    SpecificationAnnotation specific,
                                                    Instance<Object> instance) {
        logger.trace("Entering ExtensionActivator : "
                     + "filteredServicesQualifiers() with parameters {} | {} | {}",
                     new Object[] {event,
                                   specific,
                                   instance});
        Set<Annotation> eventQualifiers = new HashSet<Annotation>();
        eventQualifiers.add(specific);
        WeldOSGiExtension extension = instance.select(WeldOSGiExtension.class).get();
        for (Annotation annotation : extension.getObservers()) {
            String value = ((Filter) annotation).value();
            try {
                org.osgi.framework.Filter filter = context.createFilter(value);
                if (filter.match(event.getReference())) {
                    eventQualifiers.add(new FilterAnnotation(value));
                }
            }
            catch(InvalidSyntaxException ex) {
                //ignore
            }
        }
        return eventQualifiers.toArray(new Annotation[eventQualifiers.size()]);
    }

}
