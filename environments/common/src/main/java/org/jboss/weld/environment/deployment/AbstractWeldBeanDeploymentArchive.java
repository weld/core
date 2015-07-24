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

import java.util.Collection;
import java.util.Collections;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.ejb.spi.EjbDescriptor;

/**
 * Implements the basic requirements of a {@link BeanDeploymentArchive} (bean
 * archive id and service registry).
 * <p/>
 * Suitable for extension by those who need to build custom
 * {@link BeanDeploymentArchive} implementations.
 *
 * @author Pete Muir
 */
public abstract class AbstractWeldBeanDeploymentArchive implements BeanDeploymentArchive {

    private final ServiceRegistry serviceRegistry;

    private final String id;

    public AbstractWeldBeanDeploymentArchive(String id) {
        this.id = id;
        this.serviceRegistry = new SimpleServiceRegistry();
    }

    @Override
    public Collection<EjbDescriptor<?>> getEjbs() {
        return Collections.emptyList();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ServiceRegistry getServices() {
        return serviceRegistry;
    }

    @Override
    public String toString() {
        return String.format("%s [id=%s]", getClass().getSimpleName(), id);
    }

}
