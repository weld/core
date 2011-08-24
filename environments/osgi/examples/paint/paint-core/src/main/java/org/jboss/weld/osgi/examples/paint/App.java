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

package org.jboss.weld.osgi.examples.paint;

import org.jboss.weld.osgi.examples.paint.gui.PaintFrame;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.jboss.weld.environment.osgi.api.events.BundleContainerEvents;
import org.jboss.weld.environment.osgi.api.events.Invalid;
import org.jboss.weld.environment.osgi.api.events.Valid;

@ApplicationScoped
public class App {

    @Inject PaintFrame frame;

    public void onStartup(@Observes BundleContainerEvents.BundleContainerInitialized event) {
        System.out.println("CDI Container for bundle "
                + event.getBundleContext().getBundle() + " started");
        frame.start();
    }

    public void onShutdown(@Observes BundleContainerEvents.BundleContainerShutdown event) {
        frame.stop();
    }

    public void validListen(@Observes Valid valid) {
        frame.start();
    }

    public void invalidListen(@Observes Invalid invalid) {
        frame.stop();
    }
}
