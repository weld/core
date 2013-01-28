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

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.logging.messages.BootstrapMessage;
import org.jboss.weld.manager.api.ExecutorServices;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;

public class ExecutorServicesFactory {

    public static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    /**
     * Default keep-alive time in seconds
     */
    public static final long DEFAULT_KEEP_ALIVE_TIME = 60;
    private static final String CONFIGURATION_FILE = "org.jboss.weld.executor.properties";

    private static final String THREAD_POOL_SIZE = "threadPoolSize";
    private static final String DEBUG = "threadPoolDebug";
    private static final String THREAD_POOL_TYPE = "threadPoolType";
    private static final String THREAD_POOL_KEEP_ALIVE_TIME = "threadPoolKeepAliveTime";

    private ExecutorServicesFactory() {
    }

    public static ExecutorServices create(ResourceLoader loader) {
        URL configuration = loader.getResource(CONFIGURATION_FILE);
        Properties properties = null;
        if (configuration != null) {
            properties = loadProperties(configuration);
        }

        final int threadPoolSize = initIntValue(properties, THREAD_POOL_SIZE, DEFAULT_THREAD_POOL_SIZE);
        final boolean debug = initBooleanValue(properties, DEBUG, false);
        final ThreadPoolType threadPoolType = initThreadPoolType(properties, THREAD_POOL_TYPE, ThreadPoolType.FIXED);
        final long threadPoolKeepAliveTime = initLongValue(properties, THREAD_POOL_KEEP_ALIVE_TIME, DEFAULT_KEEP_ALIVE_TIME);

        if (debug) {
            return enableDebugMode(constructExecutorServices(threadPoolType, threadPoolSize, threadPoolKeepAliveTime));
        } else {
            return constructExecutorServices(threadPoolType, threadPoolSize, threadPoolKeepAliveTime);
        }
    }

    private static ExecutorServices constructExecutorServices(ThreadPoolType type, int threadPoolSize, long threadPoolKeepAliveTime) {
        switch (type) {
            case NONE: return null;
            case SINGLE_THREAD: return new SingleThreadExecutorServices();
            case FIXED_TIMEOUT: return new TimingOutFixedThreadPoolExecutorServices(threadPoolSize, threadPoolKeepAliveTime);
            default: return new FixedThreadPoolExecutorServices(threadPoolSize);
        }
    }

    private static ExecutorServices enableDebugMode(ExecutorServices executor) {
        if (executor == null) {
            return executor;
        }
        return new ProfilingExecutorServices(executor);
    }

    private static Properties loadProperties(URL url) {
        Properties properties = new Properties();
        try {
            properties.load(url.openStream());
        } catch (IOException e) {
            throw new ResourceLoadingException(e);
        }
        return properties;
    }

    private static int initIntValue(Properties properties, String property, int defaultValue) {
        if (properties == null || properties.get(property) == null) {
            return defaultValue;
        }
        String value = properties.getProperty(property);
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new DeploymentException(BootstrapMessage.INVALID_THREAD_POOL_SIZE, value);
        }
    }

    private static long initLongValue(Properties properties, String property, long defaultValue) {
        if (properties == null || properties.get(property) == null) {
            return defaultValue;
        }
        String value = properties.getProperty(property);
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw new DeploymentException(BootstrapMessage.INVALID_PROPERTY_VALUE, property, value);
        }
    }

    private static boolean initBooleanValue(Properties properties, String property, boolean defaultValue) {
        if (properties == null || properties.get(property) == null) {
            return defaultValue;
        }
        return Boolean.valueOf(properties.getProperty(property));
    }

    private static ThreadPoolType initThreadPoolType(Properties properties, String property, ThreadPoolType defaultValue) {
        if (properties == null || properties.get(property) == null) {
            return defaultValue;
        }
        String value = properties.getProperty(property);
        try {
            return ThreadPoolType.valueOf(value);
        } catch (NumberFormatException e) {
            throw new DeploymentException(BootstrapMessage.INVALID_THREAD_POOL_TYPE, value);
        }
    }

    /**
     *
     * @author Martin Kouba
     */
    public enum ThreadPoolType {
        FIXED, FIXED_TIMEOUT, NONE, SINGLE_THREAD ;
    }
}
