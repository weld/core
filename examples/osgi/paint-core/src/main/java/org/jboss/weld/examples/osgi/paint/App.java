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

package org.jboss.weld.examples.osgi.paint;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.weld.examples.osgi.paint.api.ShapeProvider;
import org.jboss.weld.examples.osgi.paint.gui.PaintFrame;
import org.ops4j.pax.cdi.api.ContainerInitialized;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

@ApplicationScoped
public class App {

    @Inject
    private PaintFrame frame;
    private ServiceTracker<ShapeProvider, ShapeProvider> serviceTracker;

    public void onStartup(@Observes ContainerInitialized event, BundleContext ctx) {
        System.out.println("CDI Container for bundle " + ctx.getBundle() + " started");
        this.serviceTracker = new ServiceTracker<ShapeProvider, ShapeProvider>(ctx, ShapeProvider.class, frame);
        serviceTracker.open();
        frame.start();
    }

    @PreDestroy
    public void preDestroy() {
        serviceTracker.close();
        frame.stop();
    }
}
