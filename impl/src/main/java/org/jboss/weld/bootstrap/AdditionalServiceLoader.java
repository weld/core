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
package org.jboss.weld.bootstrap;

import java.util.HashSet;
import java.util.Set;

import jakarta.annotation.Priority;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.resources.DefaultResourceLoader;
import org.jboss.weld.resources.WeldClassLoaderResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.ServiceLoader;
import org.jboss.weld.util.Services;

/**
 * Loads {@link Service} implementations using {@link ServiceLoader}.
 *
 * <p>
 * This can be used e.g. by a JTA library in weld-servlet environment to provide
 * {@link org.jboss.weld.transaction.spi.TransactionServices} implementation which
 * would not otherwise be provided by weld-servlet. Deployment classloaders, TCCL and Weld's classloader are used to discover
 * service implementations. If a
 * service provider is loadable from multiple classloaders (most likely in a Java SE application) the service class will be
 * instantiated multiple times but only
 * the first instance will be used (see {@link Services#put(ServiceRegistry, Class, Service)}).
 * </p>
 *
 * <p>
 * Service implementation may specify their priority using {@link Priority}. Services with higher priority have precedence.
 * Services that do not specify
 * priority have the default priority of 4500.
 * </p>
 *
 * <p>
 * Note that the loaded services are registered globally, i.e. are accessible from each bean manager in the application.
 * </p>
 *
 * <p>
 * See <a href="https://issues.jboss.org/browse/WELD-1495">WELD-1495</a> for details.
 * </p>
 *
 * @author Jozef Hartinger
 */
class AdditionalServiceLoader {

    private final Deployment deployment;

    AdditionalServiceLoader(Deployment deployment) {
        this.deployment = deployment;
    }

    /**
     * Discovers additional services using {@link ServiceLoader} and appends them to the given service registry.
     *
     * @param registry the given service registry
     */
    void loadAdditionalServices(ServiceRegistry registry) {
        for (ResourceLoader loader : getResourceLoaders()) {
            for (Metadata<Service> metadata : ServiceLoader.load(Service.class, loader)) {
                Service service = metadata.getValue();
                for (Class<? extends Service> serviceInterface : Services.identifyServiceInterfaces(service.getClass(),
                        new HashSet<>())) {
                    Services.put(registry, serviceInterface, service);
                }
            }
        }
    }

    private Set<ResourceLoader> getResourceLoaders() {
        Set<ResourceLoader> resourceLoaders = new HashSet<>();
        for (BeanDeploymentArchive archive : deployment.getBeanDeploymentArchives()) {
            ResourceLoader resourceLoader = archive.getServices().get(ResourceLoader.class);
            if (resourceLoader != null) {
                resourceLoaders.add(resourceLoader);
            }
        }
        resourceLoaders.add(WeldClassLoaderResourceLoader.INSTANCE);
        resourceLoaders.add(DefaultResourceLoader.INSTANCE);
        return resourceLoaders;
    }
}
