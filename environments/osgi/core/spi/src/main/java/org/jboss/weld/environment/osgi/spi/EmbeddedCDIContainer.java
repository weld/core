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

import org.jboss.weld.environment.osgi.api.events.InterBundleEvent;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;

/**
 * <p>This interface represents an iterable list of CDI containers used by
 * CDI-OSGi.</p>
 * <p>It allows to: <ul>
 * <li>
 * <p>Navigate through the list of CDI containers as an
 * {@link Iterable},</p>
 * </li>
 * <li>
 * <p>Obtain the number of CDI containers,</p>
 * </li>
 * <li>
 * <p>Select a specific container by its bundle,</p>
 * </li>
 * <li>
 * <p>Start and stop the selected CDI container,</p>
 * </li>
 * <li>
 * <p>Obtain the state of the selected CDI container,</p>
 * </li>
 * <li>
 * <p>Obtain the corresponding {@link org.osgi.framework.Bundle},
 * {@link javax.enterprise.inject.spi.BeanManager}, {@link javax.enterprise.event.Event}, managed bean
 * {@link Class} and {@link javax.enterprise.inject.Instance} and registred
 * service as {@link org.osgi.framework.ServiceRegistration},</p>
 * </li>
 * <li>
 * <p>Fire {@link InterBundleEvent}.</p>
 * </li>
 * </ul></p>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 * @see CDIContainerFactory
 * @see Iterable
 * @see org.osgi.framework.Bundle
 * @see javax.enterprise.inject.spi.BeanManager
 * @see javax.enterprise.inject.Instance
 * @see javax.enterprise.event.Event
 * @see org.osgi.framework.ServiceRegistration
 * @see InterBundleEvent
 */
public interface EmbeddedCDIContainer
{
   /**
    * Test if the CDI container is on and initialized.
    *
    * @return true if the CDI container is started, false otherwise.
    */
   boolean isStarted();

   /**
    * Fire an {@link InterBundleEvent} from the {@link org.osgi.framework.Bundle} of this {@link EmbeddedCDIContainer}.
    *
    * @param event the {@link InterBundleEvent} to fire.
    */
   void fire(InterBundleEvent event);

   /**
    * Obtain the {@link javax.enterprise.inject.spi.BeanManager} of this {@link EmbeddedCDIContainer}.
    *
    * @return the {@link javax.enterprise.inject.spi.BeanManager} of this {@link EmbeddedCDIContainer}.
    */
   BeanManager getBeanManager();

   /**
    * Obtain the {@link javax.enterprise.event.Event} of this {@link EmbeddedCDIContainer}.
    *
    * @return the {@link javax.enterprise.event.Event} of this {@link EmbeddedCDIContainer}.
    */
   Event getEvent();

   /**
    * Obtain the managed bean {@link javax.enterprise.inject.Instance} of this {@link EmbeddedCDIContainer}.
    *
    * @return the managed bean {@link javax.enterprise.inject.Instance} of this {@link EmbeddedCDIContainer}.
    */
   Instance<Object> getInstance();
}
