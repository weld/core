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

import org.osgi.framework.ServiceRegistration;

import java.util.List;

/**
 * <p>This interface represents the bindings between a service and its registered
 * implementations. It is used by {@link Registration} to maintain the list of
 * registration bindings. It uses OSGi {@link ServiceRegistration}.</p>
 * <p>It allows to:<ul>
 * <li>
 * <p>Wrap a list of {@link ServiceRegistration} as binding between
 * a service and its implementations as a {@link java.util.List},</p>
 * </li>
 * <li>
 * <p>Handle this list with addition, removal, clearing and size
 * operations.</p>
 * </li>
 * </ul></p>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 * @see Registration
 * @see ServiceRegistration
 */
public interface RegistrationHolder {
    /**
     * Get all service registration in this registration holder.
     *
     * @return the list of {@link ServiceRegistration} in this holder.
     */
    List<ServiceRegistration> getRegistrations();

    /**
     * Add a service registration in this registration holder.
     *
     * @param registration the {@link ServiceRegistration} to add to this holder.
     */
    void addRegistration(ServiceRegistration registration);

    /**
     * Remove a service registration from this registration holder.
     *
     * @param registration the {@link ServiceRegistration} to remove from this holder.
     */
    void removeRegistration(ServiceRegistration registration);

    /**
     * Clear this registration holder, removing all its contained {@link ServiceRegistration}s.
     */
    void clear();

    /**
     * Get the number of service registrations in this registration holder.
     *
     * @return the number of {@link ServiceRegistration} in this holder.
     */
    int size();

}
