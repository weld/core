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

import static org.jboss.weld.environment.util.Reflections.cast;
import static org.jboss.weld.environment.util.Reflections.classForName;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.environment.logging.CommonLogger;
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
     * @param initialBeanDefiningAnnotations
     * @return the discovery strategy
     */
    public static DiscoveryStrategy create(ResourceLoader resourceLoader, Bootstrap bootstrap, Set<Class<? extends Annotation>> initialBeanDefiningAnnotations) {
        if (Reflections.isClassLoadable(resourceLoader, JANDEX_INDEX_CLASS_NAME)) {
            CommonLogger.LOG.usingJandex();
            try {
                return cast(classForName(resourceLoader, JANDEX_DISCOVERY_STRATEGY_CLASS_NAME).getConstructor(ResourceLoader.class, Bootstrap.class, Set.class)
                        .newInstance(resourceLoader, bootstrap, initialBeanDefiningAnnotations));
            } catch (Exception e) {
                throw CommonLogger.LOG.unableToInstantiate(JANDEX_DISCOVERY_STRATEGY_CLASS_NAME,
                        Arrays.toString(new Object[] { resourceLoader, bootstrap, initialBeanDefiningAnnotations }), e);
            }
        }
        return new ReflectionDiscoveryStrategy(resourceLoader, bootstrap, initialBeanDefiningAnnotations);
    }

}
