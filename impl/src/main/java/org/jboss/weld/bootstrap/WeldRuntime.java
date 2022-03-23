/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentMap;

import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.context.Destroyed;

import jakarta.enterprise.event.Shutdown;
import jakarta.enterprise.inject.Any;
import org.jboss.weld.Container;
import org.jboss.weld.ContainerState;
import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.events.BeforeShutdownImpl;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.context.ApplicationContext;
import org.jboss.weld.context.SingletonContext;
import org.jboss.weld.event.ContextEvent;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * @author Pete Muir
 * @author Ales Justin
 * @author Marko Luksa
 */
public class WeldRuntime {

    private BeanManagerImpl deploymentManager;
    private ConcurrentMap<BeanDeploymentArchive, BeanManagerImpl> bdaToBeanManagerMap;
    private String contextId;

    public WeldRuntime(String contextId, BeanManagerImpl deploymentManager, ConcurrentMap<BeanDeploymentArchive, BeanManagerImpl> bdaToBeanManagerMap) {
        this.contextId = contextId;
        this.deploymentManager = deploymentManager;
        this.bdaToBeanManagerMap = bdaToBeanManagerMap;
    }

    public BeanManagerImpl getManager(BeanDeploymentArchive beanDeploymentArchive) {
        return bdaToBeanManagerMap.get(beanDeploymentArchive);
    }

    public void shutdown() {
        try {
            // fire Shutdown event for all modules first
            Environment env = Container.getEnvironment();
            if (env != null && env.automaticallyHandleStartupShutdownEvents()) {
                fireEventForAllModules(Shutdown.class, new Shutdown(), Any.Literal.INSTANCE);
            }
            // The container must destroy all contexts.
            // For non-web modules, fire @BeforeDestroyed event
            fireEventForNonWebModules(Object.class, ContextEvent.APPLICATION_BEFORE_DESTROYED, BeforeDestroyed.Literal.APPLICATION);
            deploymentManager.instance().select(ApplicationContext.class).get().invalidate();
            deploymentManager.instance().select(SingletonContext.class).get().invalidate();

        } finally {
            // fire @Destroyed(ApplicationScope.class) for non-web modules
            fireEventForNonWebModules(Object.class, ContextEvent.APPLICATION_DESTROYED, Destroyed.Literal.APPLICATION);
            try {
                // Finally, the container must fire an event of type BeforeShutdown.
                BeforeShutdownImpl.fire(deploymentManager);
            } finally {
                Container container = Container.instance(contextId);
                container.setState(ContainerState.SHUTDOWN);
                container.cleanup();
            }
        }
    }

    /**
     * Fires given event for non-web modules. Used for @BeforeDestroyed and @Destroyed events.
     */
    private void fireEventForNonWebModules(Type eventType, Object event, Annotation... qualifiers) {
        try {
                BeanDeploymentModules modules = deploymentManager.getServices().get(BeanDeploymentModules.class);
                if (modules != null) {
                    // fire event for non-web modules
                    // web modules are handled by HttpContextLifecycle
                    for (BeanDeploymentModule module : modules) {
                        if (!module.isWebModule()) {
                            module.fireEvent(eventType, event, qualifiers);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
    }

    /**
     * Fires given event for all modules. Used for @Shutdown event.
     */
    private void fireEventForAllModules(Type eventType, Object event, Annotation... qualifiers) {
        try {
            BeanDeploymentModules modules = deploymentManager.getServices().get(BeanDeploymentModules.class);
            if (modules != null) {
                // fire event for non-web modules
                // web modules are handled by HttpContextLifecycle
                for (BeanDeploymentModule module : modules) {
                    module.fireEvent(eventType, event, qualifiers);
                }
            }
        } catch (Exception ignored) {
        }
    }
}
