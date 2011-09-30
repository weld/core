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
package org.jboss.weld.environment.osgi.spi;

import org.jboss.weld.environment.osgi.api.events.InterBundleEvent;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import java.util.Collection;

/**
 * This interface represents the CDI containers that Weld-OSGi provides to
 * bean bundles. Every managed bean {@link Bundle} gets such a CDI container.
 * <p/>
 * It defines the behavior of such a container. The Weld-OSGi integration bundle
 * implements this interface with a Weld container.
 * These CDI container are produced by a CDI container factory service.
 * <p/>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 *
 * @see CDIContainerFactory
 */
public interface CDIContainer extends EmbeddedCDIContainer {

    /**
     * Initialize and launch the CDI container. After this method call the
     * container may be started and CDI enabled in the bean bundle.
     *
     * @return true if the CDI container is initialized, false if anything goes
     *         wrong.
     */
    boolean initialize();

    /**
     * Set the CDI container to the state ready. After this method call the
     * container may be started and Weld-OSGi enabled in the bean bundle. The
     * container must be stated before this method call.
     *
     * @return true if the CDI container is ready, false if anything goes
     *         wrong.
     */
    boolean setReady();

    /**
     * Wait for the CDI container to be started and ready.
     */
    void waitToBeReady();

    /**
     * Shutdown the CDI container. After this method call the container may be
     * stopped and Weld-OSGi and CDI disabled in the bean bundle.
     *
     * @return true if the CDI container is off, false if anything goes wrong.
     */
    boolean shutdown();

    /**
     * Test if the CDI container has been initialized. CDI is enabled in the
     * bean bundle if the container is initialized.
     *
     * @return true if the CDI container is started, false otherwise.
     */
    boolean isStarted();

    /**
     * Test if the CDI container is ready. Weld-OSGi is enabled in the
     * bean bundle if the container is initialized.
     *
     * @return true if the CDI container is ready, false otherwise.
     */
    boolean isReady();

    /**
     * Fire an {@link InterBundleEvent} from the
     * {@link org.osgi.framework.Bundle} of this {@link CDIContainer}.
     *
     * @param event the {@link InterBundleEvent} to fire.
     */
    void fire(InterBundleEvent event);

    /**
     * Get the {@link org.osgi.framework.Bundle} corresponding to this
     * {@link CDIContainer}.
     *
     * @return the {@link org.osgi.framework.Bundle} corresponding to this
     *         {@link CDIContainer}.
     */
    Bundle getBundle();

    /**
     * Get the {@link javax.enterprise.inject.spi.BeanManager} of this
     * {@link CDIContainer}.
     *
     * @return the {@link javax.enterprise.inject.spi.BeanManager} of this
     *         {@link CDIContainer}.
     */
    BeanManager getBeanManager();

    /**
     * Get the {@link javax.enterprise.event.Event} of this
     * {@link CDIContainer}.
     *
     * @return the {@link javax.enterprise.event.Event} of this
     *         {@link CDIContainer}.
     */
    Event getEvent();

    /**
     * Get the {@link javax.enterprise.inject.Instance} of this
     * {@link CDIContainer}.
     *
     * @return the {@link javax.enterprise.inject.Instance} of this
     *         {@link CDIContainer}.
     */
    Instance<Object> getInstance();

    /**
     * Get the managed bean classes names of this {@link CDIContainer}.
     *
     * @return the managed bean classes names of this {@link CDIContainer} as a
     *         {@link java.util.Collection} of {@link String}.
     */
    Collection<String> getBeanClasses();

    /**
     * Get the {@link org.osgi.framework.ServiceRegistration}s of this
     * {@link CDIContainer}.
     *
     * @return the {@link org.osgi.framework.ServiceRegistration}s of this
     *         {@link CDIContainer} as a {@link java.util.Collection}.
     */
    Collection<ServiceRegistration> getRegistrations();

    /**
     * Set the {@link org.osgi.framework.ServiceRegistration}s for this
     * {@link CDIContainer}.
     *
     * @param registrations the {@link org.osgi.framework.ServiceRegistration}s
     *                      for this {@link CDIContainer} as a {@link java.util.Collection}.
     */
    void setRegistrations(Collection<ServiceRegistration> registrations);
}
