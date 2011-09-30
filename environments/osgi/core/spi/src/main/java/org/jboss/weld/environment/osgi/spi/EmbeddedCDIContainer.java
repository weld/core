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

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import org.osgi.framework.Bundle;

/**
 * This interface represents the embedded CDI containers that Weld-OSGi
 * provides to bean bundles. Every unmanaged bean {@link Bundle} gets such an
 * embedded CDI container.
 * <p/>
 * It defines the behavior of such a container. The Weld-OSGi extension bundle
 * use this interface to provide embedded container to unmanaged bean bundle.
 * These CDI container are produced by a CDI container factory service.
 * <p/>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public interface EmbeddedCDIContainer {

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

}
