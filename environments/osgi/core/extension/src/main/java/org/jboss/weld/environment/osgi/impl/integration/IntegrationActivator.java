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

import org.jboss.weld.environment.osgi.api.events.BundleContainerEvents;
import org.jboss.weld.environment.osgi.api.events.Invalid;
import org.jboss.weld.environment.osgi.impl.extension.beans.BundleHolder;
import org.jboss.weld.environment.osgi.impl.extension.beans.ContainerObserver;
import org.jboss.weld.environment.osgi.impl.extension.service.WeldOSGiExtension;
import org.jboss.weld.environment.osgi.spi.CDIContainer;
import org.jboss.weld.environment.osgi.spi.CDIContainerFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jboss.weld.environment.osgi.impl.extension.beans.RegistrationsHolderImpl;

/**
 * This is the activator of the Weld-OSGi integration part.
 * It starts with the extension originBundle.
 * <p/>
 * It looks for a CDI container factory service before it starts managing bean
 * bundles. It monitors bundle and service events to manage/unmanage
 * arriving/departing bean bundle and to start/stop when a CDI container
 * factory service arrives/leaves.
 * <p/>
 * @see org.jboss.weld.environment.osgi.impl.Activator
 *
 * @author Guillaume Sauthier
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class IntegrationActivator implements BundleActivator, BundleTrackerCustomizer, SingleServiceTracker.Listener<CDIContainerFactory> {

    private static Logger logger = LoggerFactory.getLogger(IntegrationActivator.class);

    private BundleContext context;

    private AtomicBoolean started = new AtomicBoolean(false);

    private Map<Long, CDIContainer> managed;

    private BundleTracker bundleTracker;

    private SingleServiceTracker<CDIContainerFactory> factoryTracker;

    private CDIContainerFactory factory;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final ConcurrentMap<Bundle, FutureTask> destroying = new ConcurrentHashMap<Bundle, FutureTask>();

    @Override
    public void start(BundleContext context) throws Exception {
        logger.trace("Entering IntegrationActivator : start() with parameter {}",
                     new Object[] {context});
        this.context = context;
        bundleTracker = new BundleTracker(context, Bundle.ACTIVE, this);
        factoryTracker = new SingleServiceTracker<CDIContainerFactory>(context, CDIContainerFactory.class, this);
        factoryTracker.open();
        logger.debug("Weld-OSGi integration part STARTED");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.trace("Entering ExtensionActivator : stop() with parameter {}",
                new Object[]{context});
        factoryTracker.close();
        logger.debug("Weld-OSGi integration part STOPPED");
    }

    /**
     * This method listens to arriving/departing service to monitor CDI
     * container factory services.
     * <p/>
     * When such a service first arrive, Weld-OSGi framework can start. Then
     * other services are store and when the active service is departing
     * Weld-OSGi framework is restarted and switch to next available service
     * (or just stop is there is none).
     * <p/>
     */
    @Override
    public void bind(CDIContainerFactory service) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                doUpdateCDIFactory();
            }
        });
    }

    @Override
    public Object addingBundle(Bundle bundle, BundleEvent event) {
        logger.debug("Bundle {} has started", bundle.getSymbolicName());
        if (started.get()) {
            startManagement(bundle);
        }
        return bundle;
    }

    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
        // Given we only track the ACTIVE state, we're not interested in
        // other state changes.
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
        logger.debug("Bundle {} is stopping", bundle.getSymbolicName());
        if (started.get()) {
            stopManagement(bundle);
        }
    }

    protected void doUpdateCDIFactory() {
        stopCDIOSGi();
        factory = factoryTracker.getService();
        if (factory != null) {
            startCDIOSGi();
        }

    }

    /**
     * This method start Weld-OSGi framework for the OSGi environment. It
     * manages all already active bean bundles and starts listening for
     * {@link BundleEvent}.
     * <p/>
     */
    public void startCDIOSGi() {
        logger.trace("Entering ExtensionActivator : startCDIOSGi() with no parameter");
        logger.info("Weld-OSGi bean bundles management STARTED");
        started.set(true);
        managed = new HashMap<Long, CDIContainer>();
        bundleTracker.open();
    }

    /**
     * This method stop Weld-OSGi framework for the OSGi environment. It
     * unmanages all bean bundles.
     * <p/>
     */
    public void stopCDIOSGi() {
        logger.trace("Entering ExtensionActivator : stopCDIOSGi() with no parameter");
        bundleTracker.close();
        started.set(false);
        logger.info("Weld-OSGi bean bundles management STOPPED");
    }

    private void startManagement(Bundle bundle) {
        if (bundle.getHeaders().get("Embedded-CDIContainer") != null
            && bundle.getHeaders().get("Embedded-CDIContainer").equals("true")) {
            return;
        }
        logger.debug("Managing {}", bundle.getSymbolicName());
        boolean set = WeldOSGiExtension.currentBundle.get() != null;
        WeldOSGiExtension.currentBundle.set(bundle.getBundleId());
        WeldOSGiExtension.currentContext.set(bundle.getBundleContext());
        CDIContainer holder = factory.createContainer(bundle);
        logger.trace("CDI container created");
        holder.initialize();
        logger.trace("CDI container initialized");
        if (holder.isStarted()) {
            logger.trace("CDI container started");
            // setting contextual information
            holder.getInstance().select(BundleHolder.class).get().setBundle(bundle);
            holder.getInstance().select(BundleHolder.class).get().setContext(bundle.getBundleContext());
            holder.getInstance().select(ContainerObserver.class).get().setContainers(factory.containers());
            holder.getInstance().select(ContainerObserver.class).get().setCurrentContainer(holder);
            // registering publishable services
            ServicePublisher publisher = new ServicePublisher(holder.getBeanClasses(),
                                                              bundle,
                                                              holder.getInstance(),
                                                              factory.getContractBlacklist());
            publisher.registerAndLaunchComponents();
            // fire container start
            holder.getBeanManager().fireEvent(new BundleContainerEvents.BundleContainerInitialized(bundle.getBundleContext()));
            // registering utility services
            Collection<ServiceRegistration> regs = holder.getInstance().select(RegistrationsHolderImpl.class).get().getRegistrations();
            BundleContext bundleContext = bundle.getBundleContext();
            try {
                regs.add(bundleContext.registerService(Event.class.getName(), holder.getEvent(), null));
                regs.add(bundleContext.registerService(BeanManager.class.getName(), holder.getBeanManager(), null));
                regs.add(bundleContext.registerService(Instance.class.getName(), holder.getInstance(), null));
            } catch(Throwable t) {// Ignore
                logger.warn("Unable to register a utility service for bundle {}: {}", bundle, t);
            }
            holder.setRegistrations(regs);
            factory.addContainer(holder);
            managed.put(bundle.getBundleId(), holder);
            logger.debug("Bundle {} is managed", bundle.getSymbolicName());
        }
        else {
            logger.debug("Bundle {} is not a bean bundle", bundle.getSymbolicName());
        }
        if (!set) {
            WeldOSGiExtension.currentBundle.remove();
        }
        WeldOSGiExtension.currentContext.remove();
        holder.setReady();
    }

    private void stopManagement(final Bundle bundle) {
        FutureTask future;
        synchronized (managed) {
            future = destroying.get(bundle);
            if (future == null) {
                final CDIContainer container = managed.remove(bundle.getBundleId());
                if (container != null) {
                    future = new FutureTask<Void>(new Runnable() {
                        public void run() {
                            try {
                                doStopManagement(bundle, container);
                            } finally {
                                synchronized (managed) {
                                    destroying.remove(bundle);
                                }
                            }
                        }
                    }, null);
                    destroying.put(bundle, future);
                }
            }
        }
        if (future != null) {
            try {
                future.run();
                future.get();
            } catch (Throwable t) {
                logger.warn("Error while destroying CDI container for bundle " + bundle, t);
            }
        }
    }

    private void doStopManagement(Bundle bundle, CDIContainer container) {
        logger.debug("Unmanaging {}", bundle.getSymbolicName());
        boolean set = WeldOSGiExtension.currentBundle.get() != null;
        WeldOSGiExtension.currentBundle.set(bundle.getBundleId());
        //BundleHolder bundleHolder = holder.getInstance().select(BundleHolder.class).get();
        RegistrationsHolderImpl regs = container.getInstance().select(RegistrationsHolderImpl.class).get();
        try {
            logger.trace("The container {} has been unregistered", container);
            logger.trace("Firing the BundleContainerEvents.BundleContainerShutdown event");
            // here singleton issue ?
            container.getBeanManager().fireEvent(new BundleContainerEvents.BundleContainerShutdown(bundle.getBundleContext()));
        }
        catch(Throwable t) {
        }
        for (ServiceRegistration reg : regs.getRegistrations()) {
            try {
                reg.unregister();
            } catch (Throwable t) {
                //t.printStackTrace();
            }
        }
        logger.trace("Firing the BundleState.INVALID event");
        container.getBeanManager().fireEvent(new Invalid());
        logger.trace("Shutting down the container {}", container);
        if (started.get()) {
            factory.removeContainer(bundle);
        }
        container.shutdown();
        logger.debug("Bundle {} is unmanaged", bundle.getSymbolicName());
        if (!set) {
            WeldOSGiExtension.currentBundle.remove();
        }
    }

}
