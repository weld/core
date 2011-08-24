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

package org.jboss.weld.osgi.examples.web.fwk;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.Instance;
import javax.servlet.Servlet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

public class HttpServiceTracker extends ServiceTracker {

    private final BundleContext bc;
    private HttpService httpService = null;
    private ServiceRegistration reg;
    private final String contextRoot;
    private final Instance<Object> instances;
    private final ClassLoader loader;

    public HttpServiceTracker(BundleContext bc, ClassLoader loader,
            Instance<Object> instances, String contextRoot) {
        super(bc, HttpService.class.getName(), null);
        this.bc = bc;
        this.contextRoot = contextRoot;
        this.instances = instances;
        this.loader = loader;
    }

    @Override
    public Object addingService(ServiceReference serviceRef) {
        httpService = (HttpService) super.addingService(serviceRef);
        registerServlets();
        registerResources();
        return httpService;
    }

    @Override
    public void removedService(ServiceReference ref, Object service) {
        if (httpService == service) {
            httpService.unregister(contextRoot + "/static");
            reg.unregister();
            httpService = null;
        }
        super.removedService(ref, service);
    }

    private void registerResources() {
        try {
            HttpContext myHttpContext = new OSGiHttpContext(loader);
            httpService.registerResources(contextRoot + "/static", "/tmp/static", myHttpContext);
        } catch (NamespaceException ex) {
            Logger.getLogger(HttpServiceTracker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void registerServlets() {
        ClassLoader actual = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(JerseyApplication.class.getClassLoader());
        try {
            Properties props = new Properties();
            props.put("alias", contextRoot);
            props.put("init.javax.ws.rs.Application", JerseyApplication.class.getName());
            reg = bc.registerService(Servlet.class.getName(),
                    new CDIJAXRSContainer(instances) , props);
        } finally {
            Thread.currentThread().setContextClassLoader(actual);
        }
    }
}
