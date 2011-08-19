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

import org.jboss.weld.environment.osgi.impl.extension.ExtensionActivator;
import org.jboss.weld.environment.osgi.impl.integration.IntegrationActivator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the {@link BundleActivator} of the extension bundle. It represents the entry point of CDI-OSGi.
 * <p/>
 * It is responsible for starting both extension and integration part of CDI-OSGi. First the extension is started, then
 * the integration.
 * It also stops both part when CDI-OSGi shutdown.
 *
 * @author Guillaume Sauthier
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class Activator implements BundleActivator {

    private Logger logger = LoggerFactory.getLogger(Activator.class);

    private BundleActivator integration = new IntegrationActivator();

    private BundleActivator extension = new ExtensionActivator();

    @Override
    public void start(BundleContext context) throws Exception {
        logger.debug("CDI-OSGi is starting ...");
        extension.start(context);
        integration.start(context);
        logger.debug("CDI-OSGi STARTED");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.debug("CDI-OSGi is stopping ...");
        integration.stop(context);
        extension.stop(context);
        logger.debug("CDI-OSGi STOPPED");
    }
}
