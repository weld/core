/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.environment.servlet;

import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * Abstract the web container setup notion.
 * e.g. Tomcat, Jetty, GAE, ...
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface Container {

    String CONTEXT_PARAM_CONTAINER_CLASS = "org.jboss.weld.environment.container.class";

    /**
     * Touch if this container can be used.
     * We should throw an exception if it cannot be used.
     *
     * @param resourceLoader the ResourceLoader to use for class-availability testing
     * @param context the container context
     * @return true if touch was successful, false or exception otherwise
     * @throws Exception for any error
     */
    boolean touch(ResourceLoader resourceLoader, ContainerContext context) throws Exception;

    /**
     * Initialize web container.
     *
     * @param context the container context
     */
    void initialize(ContainerContext context);

    /**
     * Destroy setup.
     *
     * @param context the container context
     */
    void destroy(ContainerContext context);
}
