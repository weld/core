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
package org.jboss.weld.environment.osgi.impl;

import org.jboss.weld.bootstrap.api.SingletonProvider;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.environment.osgi.spi.CDIContainerFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the {@link BundleActivator} of the Weld implementation bundle.
 * <p/>
 * It is responsible for providing a {@link CDIContainerFactory} service using
 * Weld as CDI implementation.
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class WeldActivator implements BundleActivator {
    private Logger logger = LoggerFactory.getLogger(WeldActivator.class);

    private CDIContainerFactory factory = new WeldCDIContainerFactory();

    private ServiceRegistration reg = null;

    @Override
    public void start(BundleContext context) throws Exception {
        logger.debug("Weld implementation bundle for Weld-OSGi is starting ...");
        SingletonProvider.initialize(new RegistrySingletonProvider());
        reg = context.registerService(CDIContainerFactory.class.getName(),
                factory,
                null);
        logger.debug("Weld implementation bundle for Weld-OSGi STARTED");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.debug("Weld implementation bundle for Weld-OSGi is stopping ...");
        reg.unregister();
        SingletonProvider.reset();
        logger.debug("Weld implementation bundle for Weld-OSGi STOPPED");
    }

}
