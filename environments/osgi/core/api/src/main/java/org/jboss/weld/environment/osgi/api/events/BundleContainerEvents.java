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
 * <p>This class wraps all the bundle container events as inner static classes.
 * There is one event class by <code>BundleContainerEventType</code>.</p>
 * <p>Each inner class allows to:<ul>
 * <li>
 * <p>Represent a specific bundle container event,</p>
 * </li>
 * <li>
 * <p>Retrieve the same information as
 * <code>AbstractBundleContainerEvent</code>.</p>
 * </li>
 * </ul></p>
 * <p>They may be used in <code>Observes</code> method in order to listen a
 * specific bundle container event.</p>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 * @see AbstractBundleContainerEvent
 * @see org.osgi.framework.Bundle
 * @see BundleContext
 * @see BundleContainerEventType
 * @see javax.enterprise.event.Observes
 */
public class BundleContainerEvents {
    /**
     * Represents the bundle events for the
     * {@link BundleContainerEventType#INITIALIZED} state.
     */
    public static class BundleContainerInitialized
            extends AbstractBundleContainerEvent {
        /**
         * Construct a new {@link BundleContainerInitialized} event.
         *
         * @param context the firing {@link BundleContext}.
         */
        public BundleContainerInitialized(BundleContext context) {
            super(context);
        }

        @Override
        public BundleContainerEventType getType() {
            return BundleContainerEventType.INITIALIZED;
        }

    }

    /**
     * Represents the bundle events for the
     * {@link BundleContainerEventType#SHUTDOWN} state.
     */
    public static class BundleContainerShutdown
            extends AbstractBundleContainerEvent {
        /**
         * Construct a new {@link BundleContainerShutdown} event.
         *
         * @param context the firing {@link BundleContext}.
         */
        public BundleContainerShutdown(BundleContext context) {
            super(context);
        }

        @Override
        public BundleContainerEventType getType() {
            return BundleContainerEventType.SHUTDOWN;
        }

    }
}
