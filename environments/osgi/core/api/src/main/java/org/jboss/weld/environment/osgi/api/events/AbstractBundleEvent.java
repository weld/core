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

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * <p>This abstract class represents all the Weld-OSGi bundle events as a
 * superclass.</p>
 * <p>It allows to:<ul>
 * <li>
 * <p>Represent all bundle events,</p>
 * </li>
 * <li>
 * <p>Retrieve the current event type as a
 * {@link BundleEventType},</p>
 * </li>
 * <li>
 * <p>Retrieve the firing bundle and its information.</p>
 * </li>
 * </ul></p>
 * <p>It may be used in {@link javax.enterprise.event.Observes} method in order
 * to listen all bundle events.</p>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 * @see Bundle
 * @see BundleEvents
 * @see BundleEventType
 * @see javax.enterprise.event.Observes
 */
public abstract class AbstractBundleEvent {
    private final Bundle bundle;

    /**
     * Construct a new bundle event for the current bundle.
     *
     * @param bundle the firing bundle (current bundle).
     */
    public AbstractBundleEvent(Bundle bundle) {
        this.bundle = bundle;
    }

    /**
     * Get the bundle event type.
     *
     * @return the {@link BundleEventType} of the fired bundle event.
     */
    public abstract BundleEventType getType();

    /**
     * Get the firing bundle id.
     *
     * @return the firing bundle id.
     */
    public long getBundleId() {
        return bundle.getBundleId();
    }

    /**
     * Get the firing bundle symbolic name.
     *
     * @return the firing bundle symbolic name.
     */
    public String getSymbolicName() {
        return bundle.getSymbolicName();
    }

    /**
     * Get the firing bundle version.
     *
     * @return the firing bundle version.
     */
    public Version getVersion() {
        return bundle.getVersion();
    }

    /**
     * Get the firing bundle.
     *
     * @return the firing bundle.
     */
    public Bundle getBundle() {
        return bundle;
    }

}
