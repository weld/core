/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.deployment;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.CDI11Deployment;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;

/**
 * Implements the basic requirements of a {@link Deployment}. Provides a service
 * registry.
 * <p/>
 * Suitable for extension by those who need to build custom {@link Deployment}
 * implementations.
 *
 * @author Pete Muir
 * @author Ales Justin
 */
public abstract class AbstractWeldDeployment implements CDI11Deployment {

    public static final String BEANS_XML = "META-INF/beans.xml";

    public static final String[] RESOURCES = { BEANS_XML };

    private final ServiceRegistry serviceRegistry;

    private final Iterable<Metadata<Extension>> extensions;

    public AbstractWeldDeployment(Bootstrap bootstrap, Iterable<Metadata<Extension>> extensions) {
        this.serviceRegistry = new SimpleServiceRegistry();
        this.extensions = extensions;
    }

    public ServiceRegistry getServices() {
        return serviceRegistry;
    }

    public Iterable<Metadata<Extension>> getExtensions() {
        return extensions;
    }

}
