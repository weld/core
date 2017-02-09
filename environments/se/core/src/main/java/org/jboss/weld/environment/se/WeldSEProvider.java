/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.CDIProvider;
import javax.enterprise.inject.spi.Unmanaged;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.environment.se.logging.WeldSELogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.cache.ComputingCache;
import org.jboss.weld.util.cache.ComputingCacheBuilder;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 *
 * @author Martin Kouba
 */
public class WeldSEProvider implements CDIProvider {

    private final ComputingCache<String, WeldContainer> containers;

    // Used for caller detection
    private final Set<String> knownClassNames;

    public WeldSEProvider() {
        this.containers = ComputingCacheBuilder.newBuilder().setWeakValues().build(new ClassNameToWeldContainer());
        ImmutableSet.Builder<String> names = ImmutableSet.builder();
        for (Class<?> clazz = getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            names.add(clazz.getName());
        }
        names.add(Unmanaged.class.getName());
        names.add(CDI.class.getName());
        this.knownClassNames = names.build();
    }

    @Override
    public CDI<Object> getCDI() {
        List<String> ids = WeldContainer.getRunningContainerIds();
        if (ids.isEmpty()) {
            return null;
        }
        if (ids.size() == 1) {
            return WeldContainer.instance(ids.get(0));
        }
        WeldSELogger.LOG.multipleContainersRunning(ids);
        String caller = getCallingClassName();
        if (caller != null) {
            return containers.getValue(caller);
        }
        // We are not able to determine the caller - return the first container initialized
        return WeldContainer.instance(ids.get(0));
    }

    @Override
    public int getPriority() {
        // The priority should be always higher than the priority of CDIProvider used in Weld Servlet
        return DEFAULT_CDI_PROVIDER_PRIORITY + 1;
    }

    private String getCallingClassName() {
        boolean outerSubclassReached = false;
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            // the method call that leads to the first invocation of this class or its subclass is considered the caller
            if (!knownClassNames.contains(element.getClassName())) {
                if (outerSubclassReached) {
                    return element.getClassName();
                }
            } else {
                outerSubclassReached = true;
            }
        }
        return null;
    }

    private static class ClassNameToWeldContainer implements Function<String, WeldContainer> {

        @Override
        public WeldContainer apply(String callerClassName) {

            List<String> ids = WeldContainer.getRunningContainerIds();

            for (String containerId : ids) {
                Container container = Container.instance(containerId);
                for (Entry<BeanDeploymentArchive, BeanManagerImpl> entry : container.beanDeploymentArchives().entrySet()) {
                    for (String className : entry.getKey().getKnownClasses()) {
                        if (className.equals(callerClassName)) {
                            return WeldContainer.instance(containerId);
                        }
                    }
                    for (Class<?> clazz : entry.getKey().getLoadedBeanClasses()) {
                        if (clazz.getName().equals(callerClassName)) {
                            return WeldContainer.instance(containerId);
                        }
                    }
                }
            }
            return WeldContainer.instance(ids.get(0));
        }
    }

}
