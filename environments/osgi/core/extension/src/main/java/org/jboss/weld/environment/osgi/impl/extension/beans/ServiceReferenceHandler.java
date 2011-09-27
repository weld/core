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
package org.jboss.weld.environment.osgi.impl.extension.beans;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.jboss.weld.environment.osgi.impl.extension.OSGiServiceProducerBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for proxy used by {@link OSGiServiceProducerBean}.Automaticaly
 * release the services after each use.
 * <p/>
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class ServiceReferenceHandler implements InvocationHandler {

    private static Logger logger =
                          LoggerFactory.getLogger(ServiceReferenceHandler.class);

    private final ServiceReference serviceReference;

    private final BundleContext registry;

    public ServiceReferenceHandler(ServiceReference serviceReference,
                                   BundleContext registry) {
        logger.trace("Entering ServiceReferenceHandler : "
                     + "ServiceReferenceHandler() with parameter {} | {}",
                     new Object[] {serviceReference, registry});
        this.serviceReference = serviceReference;
        this.registry = registry;
        logger.debug("New ServiceReferenceHandler constructed {}", this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.trace("Call on the ServiceReferenceHandler {} for method {}",
                     this,
                     method);
        Object instanceToUse = registry.getService(serviceReference);
        try {
            return method.invoke(instanceToUse, args);
        }
        finally {
            registry.ungetService(serviceReference);
        }
    }

}
