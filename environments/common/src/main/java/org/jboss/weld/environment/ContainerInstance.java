/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment;

import jakarta.enterprise.inject.spi.BeanManager;

/**
 * General marker interface for a running Weld container.
 *
 * @author Jozef Hartinger
 *
 */
public interface ContainerInstance {

    /**
     * Returns a BeanManager used by this container. If the container uses multiple BeanManager instances it is not defined
     * which of them is returned.
     *
     * @return beanManager
     */
    BeanManager getBeanManager();

    /**
     * The identifier of the container
     *
     * @return identifier of the container
     */
    String getId();

    /**
     * Shuts down the container. This action may not be available in every environment.
     */
    void shutdown();
}
