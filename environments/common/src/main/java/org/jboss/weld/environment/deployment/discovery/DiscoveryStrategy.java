/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014-2019 Red Hat, Inc. and/or its affiliates, and individual
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
import java.util.Set;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.jboss.weld.resources.spi.ClassFileServices;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * This construct is not thread-safe.
 *
 * @author Matej Briškár
 * @author Martin Kouba
 * @author <a href="https://about.me/lairdnelson"
 *         target="_parent">Laird Nelson</a>
 */
public interface DiscoveryStrategy {

    /**
     * Installs a {@link ResourceLoader} for use by the
     * implementation.
     *
     * @param resourceLoader the {@link ResourceLoader} to install
     */
    void setResourceLoader(ResourceLoader resourceLoader);

    /**
     * Installs a {@link Bootstrap} for use by the
     * implementation.
     *
     * @param bootstrap the {@link Bootstrap} to install
     */
    void setBootstrap(Bootstrap bootstrap);

    /**
     * Installs the {@link Set} of bean defining annotations that the
     * implementation may use when discovering beans.
     *
     * @param initialBeanDefiningAnnotations the initial {@link Set}
     *        of bean defining annotations
     */
    void setInitialBeanDefiningAnnotations(Set<Class<? extends Annotation>> initialBeanDefiningAnnotations);

    /**
     * Optionally, a client may set a custom scanner implementation. If not set, the impl is allowed to use anything it
     * considers appropriate.
     *
     * @param beanArchiveScanner
     */
    void setScanner(BeanArchiveScanner beanArchiveScanner);

    /**
     * Register additional {@link BeanArchiveHandler} for handling discovered bean archives.
     *
     * @param handler the handler
     */
    void registerHandler(BeanArchiveHandler handler);

    /**
     *
     * @return the set of discovered {@link WeldBeanDeploymentArchive}s
     */
    Set<WeldBeanDeploymentArchive> performDiscovery();

    /**
     *
     * @return the associated {@link ClassFileServices} or <code>null</code>
     */
    ClassFileServices getClassFileServices();

}
