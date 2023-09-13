/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.unit.deployment.structure.resolution;

import java.util.Collection;
import java.util.Collections;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.resources.DefaultResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoader;

public class MockDeployment implements Deployment {

    private final ServiceRegistry services;

    public MockDeployment(ServiceRegistry services) {
        super();
        this.services = services;
    }

    @Override
    public Collection<BeanDeploymentArchive> getBeanDeploymentArchives() {
        return Collections.<BeanDeploymentArchive> singleton(new BeanDeploymentArchive() {

            @Override
            public ServiceRegistry getServices() {
                SimpleServiceRegistry registry = new SimpleServiceRegistry();
                registry.add(ResourceLoader.class, DefaultResourceLoader.INSTANCE);
                return registry;
            }

            @Override
            public String getId() {
                return "foo";
            }

            @Override
            public Collection<EjbDescriptor<?>> getEjbs() {
                return Collections.emptyList();
            }

            @Override
            public BeansXml getBeansXml() {
                return null;
            }

            @Override
            public Collection<BeanDeploymentArchive> getBeanDeploymentArchives() {
                return Collections.emptyList();
            }

            @Override
            public Collection<String> getBeanClasses() {
                return Collections.emptyList();
            }
        });
    }

    @Override
    public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
        return null;
    }

    @Override
    public ServiceRegistry getServices() {
        return services;
    }

    @Override
    public Iterable<Metadata<Extension>> getExtensions() {
        return Collections.<Metadata<Extension>> emptyList();
    }

}
