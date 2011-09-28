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
package org.jboss.weld.environment.osgi.spi;

import org.osgi.framework.Bundle;

import java.util.Collection;
import java.util.Set;

/**
 * <p>This interface represents a CDI container factory used by Weld-OSGi in
 * order to obtain {@link CDIContainer}.</p>
 * <p>It allows to: <ul>
 * <li>
 * <p>Obtain the CDI container of a specific bean
 * {@link org.osgi.framework.Bundle} (singleton for each bean bundle),</p>
 * </li>
 * <li>
 * <p>Provide a interface black list for service publishing,</p>
 * </li>
 * <li>
 * <p>Obtain the ID of the used CDI implementation.</p>
 * </li>
 * </ul></p>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 * @see CDIContainer
 * @see org.osgi.framework.Bundle
 */
public interface CDIContainerFactory {
    /**
     * Obtain the ID of the used CDI implementation.
     *
     * @return the ID of the used CDI implementation.
     */
    String getID();

    /**
     * Obtain the interface black list for service publishing,
     *
     * @return the interface black list for service publishing as a
     *         {@link java.util.List} of {@link String}.
     */
    Set<String> getContractBlacklist();

    /**
     * Obtain the singleton {@link CDIContainer} for the given bundle.
     *
     * @param bundle the {@link org.osgi.framework.Bundle} which
     *               {@link CDIContainer} is wanted.
     * @return the {@link CDIContainer} for the given
     *         {@link org.osgi.framework.Bundle}.
     */
    CDIContainer createContainer(Bundle bundle);

    /**
     * Obtain the singleton {@link CDIContainer} for the given bundle.
     *
     * @param bundle the {@link org.osgi.framework.Bundle} which
     *               {@link CDIContainer} is wanted.
     * @return the {@link CDIContainer} for the given
     *         {@link org.osgi.framework.Bundle}.
     */
    CDIContainer container(Bundle bundle);

    void removeContainer(Bundle bundle);

    void addContainer(CDIContainer container);

    /**
     * Obtaint all {@link CDIContainer}s.
     *
     * @return all {@link CDIContainer}s.
     */
    Collection<CDIContainer> containers();

}
