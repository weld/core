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
package org.jboss.weld.environment.osgi.ee;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.weld.environment.osgi.api.events.BundleContainerEvents;
import org.jboss.weld.environment.osgi.api.events.Invalid;
import org.jboss.weld.environment.osgi.impl.extension.beans.BundleHolder;
import org.jboss.weld.environment.osgi.impl.extension.beans.ContainerObserver;
import org.jboss.weld.environment.osgi.impl.extension.beans.RegistrationsHolderImpl;
import org.jboss.weld.environment.osgi.impl.extension.beans.ServiceRegistryImpl;
import org.jboss.weld.environment.osgi.impl.extension.service.WeldOSGiExtension;
import org.jboss.weld.environment.osgi.impl.integration.ServicePublisher;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

@Singleton
@Startup
public class BundleContextAccessor {

    @Inject
    private WeldOSGiExtension ext;
    @Resource
    private BundleContext injectedContext;
    private BundleContext actualContext;
    @Inject
    private BundleHolder holder;
    @Inject
    private ServiceRegistryImpl sr;
    @Inject
    private ContainerObserver observer;
    @Inject
    private Event<Invalid> invalid;
    @Inject
    private Event<BundleContainerEvents.BundleContainerInitialized> init;
    @Inject
    private Event<BundleContainerEvents.BundleContainerShutdown> shutdown;
    @Inject
    private RegistrationsHolderImpl registrations;
    @Inject
    @Any
    private Instance<Object> instance;

    @PostConstruct
    public void start() {
        BundleContext bc = null;
        try {
            Context context = new InitialContext();
            bc = (BundleContext) context.lookup("java:comp/BundleContext");
        } catch (NamingException ex) {
        }
        if (bc == null) {
            if (injectedContext != null) {
                actualContext = injectedContext;
            } else {
                throw new RuntimeException("Can't start Weld-OSGi in hybrid mode.");
            }
        } else {
            actualContext = bc;
        }
        ext.startHybridMode(actualContext);
        holder.setBundle(injectedContext.getBundle());
        holder.setContext(injectedContext);
//        ServiceReference factoryRef = actualContext.getServiceReference(CDIContainerFactory.class.getName());
//        if (factoryRef != null) {
//            CDIContainerFactory factory = (CDIContainerFactory) actualContext.getService(factoryRef);
//            observer.setContainers(factory.containers());
//        }
        ServicePublisher publisher = new ServicePublisher(ext.getPublishableClasses(),
                actualContext.getBundle(),
                instance);
        publisher.registerAndLaunchComponents();
        init.fire(new BundleContainerEvents.BundleContainerInitialized(actualContext));
        //sr.listenStartup(null);
    }

    @PreDestroy
    public void stop() {
        shutdown.fire(new BundleContainerEvents.BundleContainerShutdown(actualContext));
        for (ServiceRegistration reg : registrations.getRegistrations()) {
            try {
                reg.unregister();
            } catch (Throwable t) {
                //t.printStackTrace();
            }
        }
        invalid.fire(new Invalid());
        ext.removeListeners();
    }
}
