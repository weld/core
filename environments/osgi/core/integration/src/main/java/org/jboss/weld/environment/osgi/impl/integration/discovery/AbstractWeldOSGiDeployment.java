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

package org.jboss.weld.environment.osgi.impl.integration.discovery;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.environment.osgi.impl.integration.OSGiProxyService;
import org.jboss.weld.serialization.spi.ProxyServices;

import javax.enterprise.inject.spi.Extension;

/**
 * Implements the basic requirements of a {@link Deployment}. Provides a service registry.
 * <p/>
 * Suitable for extension by those who need to build custom {@link Deployment} implementations.
 *
 * @author Pete Muir
 */
public abstract class AbstractWeldOSGiDeployment implements Deployment {

    private final ServiceRegistry serviceRegistry;
    private final Iterable<Metadata<Extension>> extensions;

    public AbstractWeldOSGiDeployment(Bootstrap bootstrap) {
        this.serviceRegistry = new SimpleServiceRegistry();
        this.serviceRegistry.add(ProxyServices.class, new OSGiProxyService());
        // OK, Here we can install our own Extensions instances
        this.extensions = bootstrap.loadExtensions(getClass().getClassLoader());
    }

    @Override
    public ServiceRegistry getServices() {
        return serviceRegistry;
    }

    @Override
    public Iterable<Metadata<Extension>> getExtensions() {
        return extensions;
    }
}
