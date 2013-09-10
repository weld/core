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

import java.util.concurrent.ConcurrentMap;

import javax.enterprise.inject.spi.CDI;

import org.jboss.weld.Container;
import org.jboss.weld.ContainerState;
import org.jboss.weld.Weld;
import org.jboss.weld.bootstrap.events.BeforeShutdownImpl;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.context.ApplicationContext;
import org.jboss.weld.context.SingletonContext;
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
        BeanManagerImpl beanManager = bdaToBeanManagerMap.get(beanDeploymentArchive);
        return beanManager == null ? null : beanManager.getCurrent();
    }

    public void shutdown() {
        try {
            // First, the container must destroy all contexts.
            deploymentManager.instance().select(ApplicationContext.class).get().invalidate();
            deploymentManager.instance().select(SingletonContext.class).get().invalidate();
        } finally {
            try {
                // Finally, the container must fire an event of type BeforeShutdown.
                BeforeShutdownImpl.fire(deploymentManager);
            } finally {
                Container container = Container.instance(contextId);
                container.setState(ContainerState.SHUTDOWN);
                container.cleanup();
                // remove BeanManager references
                try {
                    CDI<?> cdi = CDI.current();
                    if (cdi instanceof Weld) {
                        ((Weld) cdi).cleanup();
                    }
                } catch (java.lang.IllegalStateException ignored) {
                }
            }
        }
    }
}
