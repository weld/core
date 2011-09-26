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

import java.lang.annotation.Annotation;

/**
 * <p>It represents a service instance producer parametrized by the service to
 * inject. It has the same behavior than CDI
 * {@link javax.enterprise.inject.Instance} except that it represents only OSGi
 * service beans.</p>
 * <p>IT allows to:<ul>
 * <li>
 * <p>Wrap a list of potential service implementations as an
 * {@link Iterable} java object,</p>
 * </li>
 * <li>
 * <p>Select a subset of these service implementations filtered by
 * {@link javax.inject.Qualifier}s or LDAP filters,</p>
 * </li>
 * <li>
 * <p>Iterate through these service implementations,</p>
 * </li>
 * <li>
 * <p>Obtain an instance of the first remaining service
 * implementations,</p>
 * </li>
 * <li>
 * <p>Obtain utility information about the contained service
 * implementations.</p>
 * </li>
 * </ul></p>
 * <p>OSGi services should not be subtyped.</p>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 * @see javax.enterprise.inject.Instance
 * @see javax.inject.Provider
 * @see Iterable
 * @see ServiceRegistry
 * @see Registration
 */
public interface Service<T> extends Iterable<T> {
    /**
     * Obtain the first service instance.
     *
     * @return an instance of the service.
     */
    T get();

    /**
     * Obtain a subset of the service implementations containing the first
     * implementation found.
     *
     * @return a subset of the service implementations as another {@link Service}.
     */
    Iterable<T> first();

    /**
     * Obtain a subset of the service implementations that matches the given
     * {@link javax.inject.Qualifier}
     *
     * @param qualifiers the filtering {@link javax.inject.Qualifier}s.
     * @return a subset of the service implementations as another {@link Service}.
     */
    Service<T> select(Annotation... qualifiers);

    /**
     * Obtain a subset of the service implementations that matches the given
     * {@link javax.inject.Qualifier}
     *
     * @param filter the filtering LDAP {@link String}.
     * @return a subset of the service implementations as another {@link Service}.
     */
    Service<T> select(String filter);

    /**
     * Test if there is no available implementation.
     *
     * @return true if there is no implementation, false otherwise.
     */
    boolean isUnsatisfied();

    /**
     * Test if there are multiple implementations.
     *
     * @return true if there are multiple implementations, false otherwise.
     */
    boolean isAmbiguous();

    /**
     * Obtain the number of available implementations
     *
     * @return the number of available implementations.
     */
    int size();

}
