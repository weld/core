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
import org.osgi.framework.ServiceReference;

/**
 * <p>This class wraps all the service events as inner static classes. There is one
 * event class by {@link ServiceEventType}.</p>
 * <p>Each inner class allows to:<ul>
 * <li>
 * <p>Represent a specific service event,</p>
 * </li>
 * <li>
 * <p>Retrieve the same information as
 * {@link AbstractServiceEvent}.</p>
 * </li>
 * </ul></p>
 * <p>They may be used in {@link javax.enterprise.event.Observes} method in
 * order to listen a specific service event.</p>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 * @see AbstractBundleEvent
 * @see org.osgi.framework.Bundle
 * @see BundleContext
 * @see ServiceReference
 * @see ServiceEventType
 * @see javax.enterprise.event.Observes
 */
public class ServiceEvents {
    /**
     * Represents the service events for the
     * {@link ServiceEventType#SERVICE_ARRIVAL} state.
     */
    public static class ServiceArrival extends AbstractServiceEvent {
        /**
         * Construct a new {@link ServiceArrival} event.
         *
         * @param ref     the firing {@link ServiceReference}
         * @param context the firing {@link BundleContext}
         */
        public ServiceArrival(
                ServiceReference ref, BundleContext context) {
            super(ref, context);
        }

        @Override
        public ServiceEventType eventType() {
            return ServiceEventType.SERVICE_ARRIVAL;
        }

    }

    /**
     * Represents the service events for the
     * {@link ServiceEventType#SERVICE_CHANGED} state.
     */
    public static class ServiceChanged extends AbstractServiceEvent {
        /**
         * Construct a new {@link ServiceChanged} event.
         *
         * @param ref     the firing {@link ServiceReference}
         * @param context the firing {@link BundleContext}
         */
        public ServiceChanged(
                ServiceReference ref, BundleContext context) {
            super(ref, context);
        }

        @Override
        public ServiceEventType eventType() {
            return ServiceEventType.SERVICE_CHANGED;
        }

    }

    /**
     * Represents the service events for the
     * {@link ServiceEventType#SERVICE_DEPARTURE} state.
     */
    public static class ServiceDeparture extends AbstractServiceEvent {
        /**
         * Construct a new {@link ServiceDeparture} event.
         *
         * @param ref     the firing {@link ServiceReference}
         * @param context the firing {@link BundleContext}
         */
        public ServiceDeparture(
                ServiceReference ref, BundleContext context) {
            super(ref, context);
        }

        @Override
        public ServiceEventType eventType() {
            return ServiceEventType.SERVICE_DEPARTURE;
        }

    }
}
