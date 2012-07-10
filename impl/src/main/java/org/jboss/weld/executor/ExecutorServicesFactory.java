/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.executor;

import org.jboss.weld.bootstrap.BootstrapConfiguration;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.logging.messages.BootstrapMessage;
import org.jboss.weld.manager.api.ExecutorServices;

public class ExecutorServicesFactory {

    private ExecutorServicesFactory() {
    }

    public static ExecutorServices create(BootstrapConfiguration configuration) {
        if (!configuration.isThreadingEnabled()) {
            return null;
        }
        if (!configuration.isConcurrentDeployerEnabled()) {
            return new SingleThreadExecutorServices();
        }

        ExecutorServices executor = null;

        switch (configuration.getThreadPoolType()) {
            case FIXED:
                executor = new FixedThreadPoolExecutorServices(configuration.getDeployerThreads());
                break;
            case CACHED:
                executor = new CachedThreadPoolExecutorServices(configuration.getDeployerThreads(), configuration.getThreadPoolKeepAliveTime());
                break;
            default:
                throw new DeploymentException(BootstrapMessage.INVALID_THREAD_POOL_TYPE, configuration.getThreadPoolType());
        }

        if (configuration.isDebug()) {
            executor = new ProfilingExecutorServices(executor);
        }
        return executor;
    }
}
