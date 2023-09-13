/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014-2019, Red Hat, Inc. and/or its affiliates, and individual
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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.environment.deployment.discovery.jandex.Jandex;
import org.jboss.weld.environment.logging.CommonLogger;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.ServiceLoader;

/**
 * @author Martin Kouba
 * @author <a href="https://about.me/lairdnelson"
 *         target="_parent">Laird Nelson</a>
 */
public final class DiscoveryStrategyFactory {

    private DiscoveryStrategyFactory() {
    }

    /**
     * @param resourceLoader
     * @param bootstrap
     * @param initialBeanDefiningAnnotations
     * @param jandexStrategyDisabled
     * @return the discovery strategy
     */
    public static DiscoveryStrategy create(ResourceLoader resourceLoader, Bootstrap bootstrap,
            Set<Class<? extends Annotation>> initialBeanDefiningAnnotations, boolean jandexStrategyDisabled,
            BeanDiscoveryMode emptyBeansXmlDiscoveryMode) {
        DiscoveryStrategy returnValue = null;
        final Iterator<Metadata<DiscoveryStrategy>> iterator = ServiceLoader.load(DiscoveryStrategy.class, resourceLoader)
                .iterator();
        if (iterator != null && iterator.hasNext()) {
            final DiscoveryStrategy candidate = iterator.next().getValue();
            if (candidate != null) {
                candidate.setResourceLoader(resourceLoader);
                candidate.setBootstrap(bootstrap);
                candidate.setInitialBeanDefiningAnnotations(initialBeanDefiningAnnotations);
                CommonLogger.LOG.usingServiceLoaderSourcedDiscoveryStrategy(candidate);
                returnValue = candidate;
            }
        } else if (Jandex.isJandexAvailable(resourceLoader)) {
            if (jandexStrategyDisabled) {
                CommonLogger.LOG.jandexDiscoveryStrategyDisabled();
            } else {
                CommonLogger.LOG.usingJandex();
                try {
                    returnValue = Jandex.createJandexDiscoveryStrategy(resourceLoader, bootstrap,
                            initialBeanDefiningAnnotations, emptyBeansXmlDiscoveryMode);
                } catch (Exception e) {
                    throw CommonLogger.LOG.unableToInstantiate(Jandex.JANDEX_DISCOVERY_STRATEGY_CLASS_NAME,
                            Arrays.toString(new Object[] { resourceLoader, bootstrap,
                                    initialBeanDefiningAnnotations, emptyBeansXmlDiscoveryMode }),
                            e);
                }
            }
        }
        if (returnValue == null) {
            returnValue = new ReflectionDiscoveryStrategy(resourceLoader, bootstrap, initialBeanDefiningAnnotations,
                    emptyBeansXmlDiscoveryMode);
        }
        return returnValue;
    }

}
