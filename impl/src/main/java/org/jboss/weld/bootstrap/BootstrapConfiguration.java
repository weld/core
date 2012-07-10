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
package org.jboss.weld.bootstrap;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.logging.messages.BootstrapMessage;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;

/**
 * Loads bootstrap configuration.
 *
 * @author Jozef Hartinger
 * @author Martin Kouba
 */
public class BootstrapConfiguration {

    public static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    /**
     * Default keep-alive time in seconds
     */
    public static final long DEFAULT_KEEP_ALIVE_TIME = 60;

    private static final String CONFIGURATION_FILE = "org.jboss.weld.bootstrap.properties";
    private static final String DEPLOYER_THREADS = "deployerThreads";
    private static final String PRELOADER_THREADS = "preloaderThreads";
    private static final String DEBUG = "debug";
    private static final String ENABLE_THREADING = "enableThreading";
    private static final String THREAD_POOL_TYPE = "threadPoolType";
    private static final String THREAD_POOL_KEEP_ALIVE_TIME = "threadPoolKeepAliveTimeSeconds";

    private final int deployerThreads;
    private final int preloaderThreads;
    private final boolean debug;
    private final boolean threadingEnabled;
    private final ThreadPoolType threadPoolType;
    private final long threadPoolKeepAliveTime;

    public BootstrapConfiguration(ResourceLoader loader) {
        URL configuration = loader.getResource(CONFIGURATION_FILE);
        Properties properties = null;
        if (configuration != null) {
            properties = loadProperties(configuration);
        }
        this.deployerThreads = initIntValue(properties, DEPLOYER_THREADS, DEFAULT_THREAD_POOL_SIZE);
        this.preloaderThreads = initIntValue(properties, PRELOADER_THREADS, Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
        this.debug = initBooleanValue(properties, DEBUG, false);
        this.threadingEnabled = initBooleanValue(properties, ENABLE_THREADING, true);
        this.threadPoolType = initThreadPoolType(properties, THREAD_POOL_TYPE, ThreadPoolType.FIXED);
        this.threadPoolKeepAliveTime = initLongValue(properties, THREAD_POOL_KEEP_ALIVE_TIME, DEFAULT_KEEP_ALIVE_TIME);
    }

    private int initIntValue(Properties properties, String property, int defaultValue) {
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

    private boolean initBooleanValue(Properties properties, String property, boolean defaultValue) {
        if (properties == null || properties.get(property) == null) {
            return defaultValue;
        }
        return Boolean.valueOf(properties.getProperty(property));
    }

    private ThreadPoolType initThreadPoolType(Properties properties, String property, ThreadPoolType defaultValue) {
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

    private long initLongValue(Properties properties, String property, long defaultValue) {
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

    private Properties loadProperties(URL url) {
        Properties properties = new Properties();
        try {
            properties.load(url.openStream());
        } catch (IOException e) {
            throw new ResourceLoadingException(e);
        }
        return properties;
    }

    public int getDeployerThreads() {
        return deployerThreads;
    }

    public int getPreloaderThreads() {
        return preloaderThreads;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isThreadingEnabled() {
        return threadingEnabled;
    }

    public boolean isConcurrentDeployerEnabled() {
        return deployerThreads > 0 && threadingEnabled;
    }

    public boolean isPreloaderEnabled() {
        return preloaderThreads > 0 && threadingEnabled;
    }

    public ThreadPoolType getThreadPoolType() {
        return threadPoolType;
    }

    public long getThreadPoolKeepAliveTime() {
        return threadPoolKeepAliveTime;
    }

    /**
     *
     * @author Martin Kouba
     */
    public enum ThreadPoolType {
        FIXED,
        CACHED,
        ;
    }
}
