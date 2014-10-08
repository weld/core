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

import javax.annotation.Priority;

import org.jboss.weld.bootstrap.api.BootstrapService;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.resources.DefaultResourceLoader;
import org.jboss.weld.resources.WeldClassLoaderResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.jboss.weld.util.ServiceLoader;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Loads {@link Service} implementations using {@link ServiceLoader}. This can be used e.g. by a JTA library
 * in weld-servlet environment to provide {@link TransactionServices} implementation which would not otherwise
 * be provided by weld-servlet. Deployment classloaders, TCCL and Weld's classloader are used to discover service
 * implementations. Service implementation may specify their priority using {@link Priority}. Services with higher
 * priority have precedence. Services that do not specify priority have the default priority of 4500.
 * See https://issues.jboss.org/browse/WELD-1495 for details.
 *
 * @author Jozef Hartinger
 *
 */
class AdditionalServiceLoader {

    private static final int DEFAULT_PLATFORM_PRIORITY = 4500;

    private final Deployment deployment;

    AdditionalServiceLoader(Deployment deployment) {
        this.deployment = deployment;
    }

    /**
     * Discovers additional services using {@link ServiceLoader} and appends them to the given service registry.
     * @param registry the given service registry
     */
    void loadAdditionalServices(ServiceRegistry registry) {
        for (ResourceLoader loader : getResourceLoaders()) {
            for (Metadata<Service> metadata : ServiceLoader.load(Service.class, loader)) {
                Service service = metadata.getValue();
                for (Class<? extends Service> serviceInterface : identifyServiceInterfaces(service.getClass(), new HashSet<Class<? extends Service>>())) {
                    put(registry, serviceInterface, service);
                }
            }
        }
    }

    /**
     * Identifies service views for a service implementation class. A service view is either: - an interface that directly extends {@link Service} or
     * {@link BootstrapService} - a clazz that directly implements {@link Service} or {@link BootstrapService}
     *
     * @param clazz the given class
     * @param serviceInterfaces a set that this method populates with service views
     * @return serviceInterfaces
     */
    private Set<Class<? extends Service>> identifyServiceInterfaces(Class<?> clazz, Set<Class<? extends Service>> serviceInterfaces) {
        if (clazz == null || Object.class.equals(clazz) || BootstrapService.class.equals(clazz)) {
            return serviceInterfaces;
        }
        for (Class<?> interfac3 : clazz.getInterfaces()) {
            if (Service.class.equals(interfac3) || BootstrapService.class.equals(interfac3)) {
                serviceInterfaces.add(Reflections.<Class<? extends Service>>cast(clazz));
            }
        }
        for (Class<?> interfac3 : clazz.getInterfaces()) {
            identifyServiceInterfaces(interfac3, serviceInterfaces);
        }
        identifyServiceInterfaces(clazz.getSuperclass(), serviceInterfaces);
        return serviceInterfaces;
    }

    private <T extends Service> void put(ServiceRegistry registry, Class<T> key, Service value) {
        Service previous = registry.get(key);
        if (previous == null) {
            BootstrapLogger.LOG.debugv("Installing additional service {0} ({1})", key.getName(), value.getClass());
            registry.add(key, Reflections.<T>cast(value));
        } else if (shouldOverride(key, previous, value)) {
            BootstrapLogger.LOG.debugv("Overriding service implementation for {0}. Previous implementation {1} is replaced with {2}", key.getName(), previous
                    .getClass().getName(), value.getClass().getName());
            registry.add(key, Reflections.<T>cast(value));
        }
    }

    protected boolean shouldOverride(Class<? extends Service> key, Service previous, Service next) {
        return getPriority(next) > getPriority(previous);
    }

    private int getPriority(Service service) {
        Priority priority = service.getClass().getAnnotation(Priority.class);
        if (priority != null) {
            return priority.value();
        }
        return DEFAULT_PLATFORM_PRIORITY;
    }

    private Set<ResourceLoader> getResourceLoaders() {
        Set<ResourceLoader> resourceLoaders = new HashSet<ResourceLoader>();
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
