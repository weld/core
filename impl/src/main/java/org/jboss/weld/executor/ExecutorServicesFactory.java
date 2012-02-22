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

    private static final String CONFIGURATION_FILE = "org.jboss.weld.executor.properties";
    private static final String ENABLED = "enabled";
    private static final String DEBUG = "debug";
    private static final String THREAD_POOL_SIZE = "threadPoolSize";

    private ExecutorServicesFactory() {
    }

    public static ExecutorServices create(ResourceLoader loader) {
        URL configuration = loader.getResource(CONFIGURATION_FILE);
        if (configuration == null) {
            return createDefault();
        } else {
            return create(loadProperties(configuration));
        }
    }

    public static ExecutorServices create(Properties properties) {
        if (properties.getProperty(ENABLED, "true").equalsIgnoreCase("false")) {
            return new SingleThreadExecutorServices();
        }

        ExecutorServices executor = null;
        if (properties.containsKey(THREAD_POOL_SIZE)) {
            executor = new FixedThreadPoolExecutorServices(parseThreadPoolSize(properties.getProperty(THREAD_POOL_SIZE)));
        } else {
            executor = new FixedThreadPoolExecutorServices();
        }

        if (properties.getProperty(DEBUG, "false").equalsIgnoreCase("true")) {
            executor = new ProfilingExecutorServices(executor);
        }

        return executor;
    }

    public static ExecutorServices createDefault() {
        return new FixedThreadPoolExecutorServices();
    }

    protected static int parseThreadPoolSize(String size) {
        try {
            return Integer.valueOf(size);
        } catch (NumberFormatException e) {
            throw new DeploymentException(BootstrapMessage.INVALID_THREAD_POOL_SIZE, size);
        }
    }

    protected static Properties loadProperties(URL url) {
        Properties properties = new Properties();
        try {
            properties.load(url.openStream());
        } catch (IOException e) {
            throw new ResourceLoadingException(e);
        }
        return properties;
    }
}
