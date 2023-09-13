/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.util;

import java.util.Set;

import jakarta.annotation.Priority;

import org.jboss.weld.bootstrap.api.BootstrapService;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.util.reflection.Reflections;

/**
 *
 * @author Martin Kouba
 * @see Service
 */
public final class Services {

    private static final int DEFAULT_PLATFORM_PRIORITY = 4500;

    private Services() {
    }

    /**
     * Identifies service views for a service implementation class. A service view is either: - an interface that directly
     * extends {@link Service} or
     * {@link BootstrapService} - a clazz that directly implements {@link Service} or {@link BootstrapService}
     *
     * @param clazz the given class
     * @param serviceInterfaces a set that this method populates with service views
     * @return serviceInterfaces
     */
    public static Set<Class<? extends Service>> identifyServiceInterfaces(Class<?> clazz,
            Set<Class<? extends Service>> serviceInterfaces) {
        if (clazz == null || Object.class.equals(clazz) || BootstrapService.class.equals(clazz)) {
            return serviceInterfaces;
        }
        for (Class<?> interfac3 : clazz.getInterfaces()) {
            if (Service.class.equals(interfac3) || BootstrapService.class.equals(interfac3)) {
                serviceInterfaces.add(Reflections.cast(clazz));
            }
        }
        for (Class<?> interfac3 : clazz.getInterfaces()) {
            identifyServiceInterfaces(interfac3, serviceInterfaces);
        }
        identifyServiceInterfaces(clazz.getSuperclass(), serviceInterfaces);
        return serviceInterfaces;
    }

    public static <T extends Service> void put(ServiceRegistry registry, Class<T> key, Service value) {
        Service previous = registry.get(key);
        if (previous == null) {
            BootstrapLogger.LOG.debugv("Installing additional service {0} ({1})", key.getName(), value.getClass());
            registry.add(key, Reflections.cast(value));
        } else if (shouldOverride(key, previous, value)) {
            BootstrapLogger.LOG.debugv(
                    "Overriding service implementation for {0}. Previous implementation {1} is replaced with {2}",
                    key.getName(),
                    previous.getClass().getName(), value.getClass().getName());
            registry.add(key, Reflections.cast(value));
        }
    }

    private static boolean shouldOverride(Class<? extends Service> key, Service previous, Service next) {
        return getPriority(next) > getPriority(previous);
    }

    private static int getPriority(Service service) {
        Priority priority = service.getClass().getAnnotation(Priority.class);
        if (priority != null) {
            return priority.value();
        }
        return DEFAULT_PLATFORM_PRIORITY;
    }

}
