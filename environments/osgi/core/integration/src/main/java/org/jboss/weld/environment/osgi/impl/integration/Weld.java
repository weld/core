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

import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.environment.osgi.impl.extension.service.WeldOSGiExtension;
import org.jboss.weld.environment.osgi.impl.integration.discovery.bundle.BundleBeanDeploymentArchiveFactory;
import org.jboss.weld.environment.osgi.impl.integration.discovery.bundle.BundleDeployment;
import org.jboss.weld.manager.api.WeldManager;
import org.osgi.framework.Bundle;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import java.util.Collection;

/**
 * Weld container used for bean bundles by
 * {@link org.jboss.weld.environment.osgi.WeldCDIContainer}.
 * <p/>
 * It is responsible for initialization of a Weld container requested by
 * Weld-OSGi using the {@link
 * org.jboss.weld.environment.osgi.WeldCDIContainerFactory}.
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class Weld {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(Weld.class);

    private final Bundle bundle;
    private boolean started = false;
    private WeldBootstrap bootstrap;
    private boolean hasShutdownBeenCalled = false;
    private BundleBeanDeploymentArchiveFactory factory;
    private WeldManager manager;
    private Collection<String> beanClasses;
    private final String id;

    public Weld(Bundle bundle) {
        logger.debug("Creation of a new Weld instance for bundle {}", bundle);
        this.bundle = bundle;
        this.id = bundle.getSymbolicName() + "-" + bundle.getBundleId();
        factory = new BundleBeanDeploymentArchiveFactory();
    }

    public boolean isStarted() {
        return started;
    }

    public boolean initialize() {
        logger.debug("Initialization of a Weld instance for bundle {}", bundle);
        started = false;
        // ugly hack to make jboss interceptors works.
        // thank you Thread.currentThread().getContextClassLoader().loadClass()
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        // -------------
        boolean set = WeldOSGiExtension.currentBundle.get() != null;
        WeldOSGiExtension.currentBundle.set(bundle.getBundleId());
        try {
            bootstrap = new WeldBootstrap();
            BundleDeployment deployment = createDeployment(bootstrap);
            BeanDeploymentArchive beanDeploymentArchive =
                    deployment.getBeanDeploymentArchive();
            if (beanDeploymentArchive == null) {
                logger.debug("Unable to generate a BeanDeploymentArchive "
                        + "for bundle {}",
                        bundle);
                return started;
            }
            logger.info("Starting Weld instance for bundle {}", bundle);
            // Set up the container
            bootstrap.startContainer(id, new OSGiEnvironment(), deployment);
            // Start the container
            bootstrap.startInitialization();
            bootstrap.deployBeans();
            bootstrap.validateBeans();
            bootstrap.endInitialization();
            // Get this Bundle BeanManager
            manager = bootstrap.getManager(beanDeploymentArchive);
            beanClasses = beanDeploymentArchive.getBeanClasses();
            started = true;
        } catch (Throwable t) {
            logger.error("Initialization of Weld instance for bundle {}"
                    + " caused an error: {}",
                    bundle,
                    t);
            t.printStackTrace();
        } finally {
            if (!set) {
                WeldOSGiExtension.currentBundle.remove();
            }
            Thread.currentThread().setContextClassLoader(old);
        }
        return started;
    }

    private BundleDeployment createDeployment(Bootstrap bootstrap) {
        return new BundleDeployment(bundle, bootstrap, factory);
    }

    public boolean shutdown() {
        if (started) {
            synchronized (this) {
                if (!hasShutdownBeenCalled) {
                    logger.info("Stopping Weld instance for bundle {}", bundle);
                    hasShutdownBeenCalled = true;
                    try {
                        bootstrap.shutdown();
                    } catch (Throwable t) {
                        logger.error("Shutdown of Weld instance for bundle {} "
                                + "caused an error: {}",
                                bundle,
                                t);
                    }
                    started = false;
                    return true;
                } else {
                    logger.warn("Skipping spurious call to shutdown"
                            + " Weld instance for bundle {}",
                            bundle);
                    return false;
                }
            }
        }
        return false;
    }

    public Event getEvent() {
        return manager.instance().select(Event.class).get();
    }

    public BeanManager getBeanManager() {
        return manager;
    }

    public Instance<Object> getInstance() {
        return manager.instance();
    }

    public Collection<String> getBeanClasses() {
        return beanClasses;
    }
}
