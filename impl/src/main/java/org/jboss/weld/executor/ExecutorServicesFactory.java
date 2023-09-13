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

import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.api.ExecutorServices;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.Permissions;

public class ExecutorServicesFactory {

    private ExecutorServicesFactory() {
    }

    public static ExecutorServices create(ResourceLoader loader, WeldConfiguration configuration) {

        final int threadPoolSize = configuration.getIntegerProperty(ConfigurationKey.EXECUTOR_THREAD_POOL_SIZE);
        final boolean debug = configuration.getBooleanProperty(ConfigurationKey.EXECUTOR_THREAD_POOL_DEBUG);
        final ThreadPoolType threadPoolType = initThreadPoolType(configuration);
        final long threadPoolKeepAliveTime = configuration
                .getLongProperty(ConfigurationKey.EXECUTOR_THREAD_POOL_KEEP_ALIVE_TIME);

        if (debug) {
            return enableDebugMode(constructExecutorServices(threadPoolType, threadPoolSize, threadPoolKeepAliveTime));
        } else {
            return constructExecutorServices(threadPoolType, threadPoolSize, threadPoolKeepAliveTime);
        }
    }

    private static ExecutorServices constructExecutorServices(ThreadPoolType type, int threadPoolSize,
            long threadPoolKeepAliveTime) {
        switch (type) {
            case NONE:
                return null;
            case SINGLE_THREAD:
                return new SingleThreadExecutorServices();
            case FIXED_TIMEOUT:
                return new TimingOutFixedThreadPoolExecutorServices(threadPoolSize, threadPoolKeepAliveTime);
            case COMMON:
                return new CommonForkJoinPoolExecutorServices();
            default:
                return new FixedThreadPoolExecutorServices(threadPoolSize);
        }
    }

    private static ExecutorServices enableDebugMode(ExecutorServices executor) {
        if (executor == null) {
            return executor;
        }
        return new ProfilingExecutorServices(executor);
    }

    private static ThreadPoolType initThreadPoolType(WeldConfiguration configuration) {

        String threadPoolTypeString = configuration.getStringProperty(ConfigurationKey.EXECUTOR_THREAD_POOL_TYPE);

        if (threadPoolTypeString.isEmpty()) {
            // do not create ExecutorServices by default if we do not have the "modifyThreadGroup" permission
            return Permissions.hasPermission(Permissions.MODIFY_THREAD_GROUP) ? ThreadPoolType.FIXED : ThreadPoolType.NONE;
        } else {
            try {
                ThreadPoolType threadPoolType = ThreadPoolType.valueOf(threadPoolTypeString);
                if (System.getSecurityManager() != null && ThreadPoolType.COMMON == threadPoolType) {
                    threadPoolType = ThreadPoolType.FIXED;
                    BootstrapLogger.LOG.commonThreadPoolWithSecurityManagerEnabled(threadPoolType);
                }
                return threadPoolType;
            } catch (Exception e) {
                throw BootstrapLogger.LOG.invalidThreadPoolType(threadPoolTypeString);
            }
        }
    }

    /**
     *
     * @author Martin Kouba
     */
    public enum ThreadPoolType {
        FIXED,
        FIXED_TIMEOUT,
        NONE,
        SINGLE_THREAD,
        COMMON
    }
}
