/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.logging.BeanManagerLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.cache.ComputingCache;
import org.jboss.weld.util.cache.ComputingCacheBuilder;

/**
 * Provides convenient way to access the CDI container.
 *
 * @author Jozef Hartinger
 *
 */
public class SimpleCDI extends AbstractCDI<Object> {

    private class ClassNameToBeanManager implements Function<String, BeanManagerProxy> {

        /**
         * Determines the correct {@link BeanManagerImpl} based on a class name of the caller.
         */
        @Override
        public BeanManagerProxy apply(String callerClassName) {
            return new BeanManagerProxy(findBeanManager(callerClassName));
        }

        public BeanManagerImpl findBeanManager(String callerClassName) {
            if (callerClassName == null) {
                throw BeanManagerLogger.LOG.unableToIdentifyBeanManager();
            }
            Set<BeanManagerImpl> managers = new HashSet<BeanManagerImpl>();
            for (Map.Entry<BeanDeploymentArchive, BeanManagerImpl> entry : container.beanDeploymentArchives().entrySet()) {
                for (String className : entry.getKey().getKnownClasses()) {
                    if (className.equals(callerClassName)) {
                        managers.add(entry.getValue());
                    }
                }
            }

            if (managers.size() == 1) {
                return managers.iterator().next();
            }
            if (managers.size() == 0) {
                return unsatisfiedBeanManager(callerClassName);
            }
            return ambiguousBeanManager(callerClassName, managers);
        }
    }

    private final ComputingCache<String, BeanManagerProxy> beanManagers;
    private final Container container;

    public SimpleCDI() {
        this(Container.instance());
    }

    public SimpleCDI(Container container) {
        this.container = container;
        beanManagers = ComputingCacheBuilder.newBuilder().setWeakValues().build(new ClassNameToBeanManager());
    }

    /**
     * Callback that allows to override the behavior when CDI.current() is not called from within a bean archive.
     */
    protected BeanManagerImpl unsatisfiedBeanManager(String callerClassName) {
        throw BeanManagerLogger.LOG.unsatisfiedBeanManager(callerClassName);
    }

    /**
     * Callback that allows to override the behavior when class that invoked CDI.current() is placed in multiple bean archives.
     */
    protected BeanManagerImpl ambiguousBeanManager(String callerClassName, Set<BeanManagerImpl> managers) {
        throw BeanManagerLogger.LOG.ambiguousBeanManager(callerClassName);
    }

    @Override
    public BeanManagerProxy getBeanManager() {
        ContainerState state = container.getState();
        if (state.equals(ContainerState.STOPPED) || state.equals(ContainerState.SHUTDOWN)) {
            throw BeanManagerLogger.LOG.beanManagerNotAvailable();
        }
        return beanManagers.getValue(getCallingClassName());
    }

    @Override
    public String toString() {
        return "Weld";
    }

    protected Container getContainer() {
        return container;
    }

    public void cleanup() {
        beanManagers.clear();
    }

}
