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

/**
 * <p>This class wraps all the bundle events as inner static classes. There is one
 * event class by {@link BundleEventType}.</p>
 * <p>Each inner class allows to:<ul>
 * <li>
 * <p>Represent a specific bundle event,</p>
 * </li>
 * <li>
 * <p>Retrieve the same information as
 * {@link AbstractBundleEvent}.</p>
 * </li>
 * </ul></p>
 * <p>They may be used in {@link javax.enterprise.event.Observes} method in
 * order to listen a specific bundle event.</p>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 * @see AbstractBundleEvent
 * @see Bundle
 * @see BundleEventType
 * @see javax.enterprise.event.Observes
 */
public class BundleEvents {
    /**
     * Represents the bundle events for the {@link BundleEventType#INSTALLED} state.
     */
    public static class BundleInstalled extends AbstractBundleEvent {
        /**
         * Construct a new {@link BundleInstalled} event.
         *
         * @param bundle the firing {@link Bundle}.
         */
        public BundleInstalled(Bundle bundle) {
            super(bundle);
        }

        @Override
        public BundleEventType getType() {
            return BundleEventType.INSTALLED;
        }

    }

    /**
     * Represents the bundle events for the
     * {@link BundleEventType#LAZY_ACTIVATION} state.
     */
    public static class BundleLazyActivation extends AbstractBundleEvent {
        /**
         * Construct a new {@link BundleLazyActivation} event.
         *
         * @param bundle the firing {@link Bundle}.
         */
        public BundleLazyActivation(Bundle bundle) {
            super(bundle);
        }

        @Override
        public BundleEventType getType() {
            return BundleEventType.LAZY_ACTIVATION;
        }

    }

    /**
     * Represents the bundle events for the {@link BundleEventType#RESOLVED}
     * state.
     */
    public static class BundleResolved extends AbstractBundleEvent {
        /**
         * Construct a new {@link BundleResolved} event.
         *
         * @param bundle the firing {@link Bundle}.
         */
        public BundleResolved(Bundle bundle) {
            super(bundle);
        }

        @Override
        public BundleEventType getType() {
            return BundleEventType.RESOLVED;
        }

    }

    /**
     * Represents the bundle events for the {@link BundleEventType#STARTED} state.
     */
    public static class BundleStarted extends AbstractBundleEvent {
        /**
         * Construct a new {@link BundleStarted} event.
         *
         * @param bundle the firing {@link Bundle}.
         */
        public BundleStarted(Bundle bundle) {
            super(bundle);
        }

        @Override
        public BundleEventType getType() {
            return BundleEventType.STARTED;
        }

    }

    /**
     * Represents the bundle events for the {@link BundleEventType#STARTING} state.
     */
    public static class BundleStarting extends AbstractBundleEvent {
        /**
         * Construct a new {@link BundleStarting} event.
         *
         * @param bundle the firing {@link Bundle}.
         */
        public BundleStarting(Bundle bundle) {
            super(bundle);
        }

        @Override
        public BundleEventType getType() {
            return BundleEventType.STARTING;
        }

    }

    /**
     * Represents the bundle events for the {@link BundleEventType#STOPPED} state.
     */
    public static class BundleStopped extends AbstractBundleEvent {
        /**
         * Construct a new {@link BundleStopped} event.
         *
         * @param bundle the firing {@link Bundle}.
         */
        public BundleStopped(Bundle bundle) {
            super(bundle);
        }

        @Override
        public BundleEventType getType() {
            return BundleEventType.STOPPED;
        }

    }

    /**
     * Represents the bundle events for the {@link BundleEventType#STOPPING} state.
     */
    public static class BundleStopping extends AbstractBundleEvent {
        /**
         * Construct a new {@link BundleStopping} event.
         *
         * @param bundle the firing {@link Bundle}.
         */
        public BundleStopping(Bundle bundle) {
            super(bundle);
        }

        @Override
        public BundleEventType getType() {
            return BundleEventType.STOPPING;
        }

    }

    /**
     * Represents the bundle events for the {@link BundleEventType#UNINSTALLED} state.
     */
    public static class BundleUninstalled extends AbstractBundleEvent {
        /**
         * Construct a new {@link BundleUninstalled} event.
         *
         * @param bundle the firing {@link Bundle}.
         */
        public BundleUninstalled(Bundle bundle) {
            super(bundle);
        }

        @Override
        public BundleEventType getType() {
            return BundleEventType.UNINSTALLED;
        }

    }

    /**
     * Represents the bundle events for the {@link BundleEventType#UNRESOLVED} state.
     */
    public static class BundleUnresolved extends AbstractBundleEvent {
        /**
         * Construct a new {@link BundleUnresolved} event.
         *
         * @param bundle the firing {@link Bundle}.
         */
        public BundleUnresolved(Bundle bundle) {
            super(bundle);
        }

        @Override
        public BundleEventType getType() {
            return BundleEventType.UNRESOLVED;
        }

    }

    /**
     * Represents the bundle events for the {@link BundleEventType#UPDATED} state.
     */
    public static class BundleUpdated extends AbstractBundleEvent {
        /**
         * Construct a new {@link BundleUpdated} event.
         *
         * @param bundle the firing {@link Bundle}.
         */
        public BundleUpdated(Bundle bundle) {
            super(bundle);
        }

        @Override
        public BundleEventType getType() {
            return BundleEventType.UPDATED;
        }

    }
}
