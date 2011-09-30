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

package org.jboss.weld.osgi.examples.standalone;

import org.jboss.weld.environment.osgi.impl.extension.service.EmbeddedContainer;
import org.jboss.weld.environment.osgi.spi.EmbeddedCDIContainer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class StandaloneActivator implements BundleActivator {

    private EmbeddedContainer embedded;
    private EmbeddedCDIContainer container;
    private MyService service;
    private MyListener listener;

    @Override
    public void start(BundleContext context) throws Exception {
        embedded = new EmbeddedContainer(context);
        container = embedded.initialize();
        service = container.getInstance().select(MyService.class).get();
        listener = container.getInstance().select(MyListener.class).get();
        System.out.println(service.hello());
        System.out.println(service.admin());
        System.out.println(service.adminAvailable());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        embedded.shutdown();
    }
}
