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

import java.util.ServiceLoader;

/**
 * Handles the reference to a bean archive.
 * <p>
 * The standard way to register a handler is via {@link DiscoveryStrategy#registerHandler(BeanArchiveHandler)}. Alternatively,
 * handlers may be registered using
 * the {@link ServiceLoader} mechanism.
 * </p>
 * <p>
 * Additionaly, handlers could specify their priority using {@code jakarta.annotation.Priority}. Handlers with higher priority
 * have precedence. The default
 * priority is 0. Handlers registered programatically have the default priority {@code registeredHandlers.size - index}, i.e.
 * derived from the order they were
 * inserted.
 * </p>
 *
 * @author Martin Kouba
 * @see DiscoveryStrategy#registerHandler(BeanArchiveHandler)
 */
public interface BeanArchiveHandler {

    /**
     * The returned builder must only contain a complete set of found classes, other properties do not have to be set.
     *
     * @param beanArchiveReference A reference to a bean archive (e.g. file path)
     * @return the BeanArchiveBuilder or <code>null</code> if the reference cannot be handled
     */
    BeanArchiveBuilder handle(String beanArchiveReference);

}
