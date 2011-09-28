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
package org.jboss.weld.environment.osgi.api.events;

import org.osgi.framework.BundleContext;

/**
 * <p>This abstract class represents all the Weld-OSGi bundle container events as a
 * superclass.</p>
 * <p>It allows to:<ul>
 * <li>
 * <p>Represent all bundle container events,</p>
 * </li>
 * <li>
 * <p>Retrieve the current event type as a
 * {@link BundleContainerEventType},</p>
 * </li>
 * <li>
 * <p>Retrieve the firing {@link BundleContext}.</p>
 * </li>
 * </ul></p>
 * <p>It may be used in {@link javax.enterprise.event.Observes} method in order
 * to listen all bundle container events.</p>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 * @see org.osgi.framework.Bundle
 * @see BundleContext
 * @see BundleContainerEvents
 * @see BundleContainerEventType
 * @see javax.enterprise.event.Observes
 */
public abstract class AbstractBundleContainerEvent {
    private BundleContext bundleContext;

    /**
     * Construct a new bundle container event for the current bundle.
     *
     * @param context the firing bundle context (current bundle context).
     */
    public AbstractBundleContainerEvent(final BundleContext context) {
        this.bundleContext = context;
    }

    /**
     * Get the firing bundle context.
     *
     * @return the firing {@link org.osgi.framework.BundleContext}.
     */
    public BundleContext getBundleContext() {
        return bundleContext;
    }

    /**
     * Get the bundle container event type.
     *
     * @return the {@link BundleContainerEventType} of the fired bundle container event.
     */
    public abstract BundleContainerEventType getType();

}
