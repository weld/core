/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.deployment.discovery;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.TypeDiscoveryConfiguration;
import org.jboss.weld.environment.util.Reflections;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 *
 * @author Martin Kouba
 */
public final class DiscoveryStrategyFactory {

    private static final String JANDEX_DISCOVERY_STRATEGY_CLASS_NAME = "org.jboss.weld.environment.deployment.discovery.jandex.JandexDiscoveryStrategy";

    private static final String JANDEX_INDEX_CLASS_NAME = "org.jboss.jandex.Index";

    private DiscoveryStrategyFactory() {
    }

    /**
     *
     * @param resourceLoader
     * @param bootstrap
     * @param typeDiscoveryConfiguration
     * @param annotatedModeSupportEnabled If <code>true</code> the resulting strategy will support bean-discovery-mode="annotated" even if jandex is not on the
     *        classpath
     * @return the discovery strategy
     */
    public static DiscoveryStrategy create(ResourceLoader resourceLoader, Bootstrap bootstrap, TypeDiscoveryConfiguration typeDiscoveryConfiguration,
            boolean annotatedModeSupportEnabled) {

        DiscoveryStrategy strategy;

        if (Reflections.isClassLoadable(JANDEX_INDEX_CLASS_NAME, resourceLoader)) {
            strategy = Reflections.newInstance(resourceLoader, JANDEX_DISCOVERY_STRATEGY_CLASS_NAME, resourceLoader, bootstrap, typeDiscoveryConfiguration);
        } else {
            if (annotatedModeSupportEnabled) {
                strategy = new ReflectionDiscoveryStrategy(resourceLoader, bootstrap, typeDiscoveryConfiguration);
            } else {
                strategy = new DefaultDiscoveryStrategy(resourceLoader, bootstrap);
            }
        }
        return strategy;
    }

}
