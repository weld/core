/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.environment.osgi.api;

/**
 * <p>This interface represents a service registry where all OSGi services may be
 * handled.</p>
 * <p>It allows to:<ul>
 * <li>
 * <p>Register a service implementation with a service, getting back the
 * corresponding {@link Registration},</p>
 * </li>
 * <li>
 * <p>Obtain the service implementations list as a
 * {@link Service}</code>.</p>
 * </li>
 * </ul></p>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 * @see Service
 * @see Registration
 */
public interface ServiceRegistry {
    /**
     * Register a service implementation.
     *
     * @param contract       the service contract interface.
     * @param implementation the service implementation class.
     * @param <T>            the service type.
     * @return the new service {@link Registration} or null if the registration
     *         goes wrong.
     */
    <T> Registration<T> registerService(Class<T> contract,
                                        Class<? extends T> implementation);

    /**
     * Register a service implementation.
     *
     * @param contract       the service contract interface.
     * @param implementation the service implementation class.
     * @param <T>            the service type.
     * @param <U>            the service implementation type.
     * @return the new service {@link Registration} or null if the registration
     *         goes wrong.
     */
    <T, U extends T> Registration<T> registerService(Class<T> contract,
                                                     U implementation);

    /**
     * Get available service implementations of a service.
     *
     * @param contract the service contract interface that implementations are
     *                 requested.
     * @param <T>      the service type.
     * @return the available service implementations as a {@link Service} or null
     *         if there is no such implementation.
     */
    <T> Service<T> getServiceReferences(Class<T> contract);

}
