/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.environment.se;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.weld.environment.se.beans.InstanceManager;

/**
 * Provides convenient access to beans and events, particularly helpful when
 * bootstrapping an application in Java SE.
 * <p>
 * An instance of this class can be obtained using the Weld class by calling:
 * <code>
 * WeldContainer weld = new Weld().initialize();
 * </code>
 * @see Weld
 *
 * @author Peter Royle
 */
public class WeldContainer
{

   private final InstanceManager instanceManager;
   private final BeanManager beanManager;

   @Inject
   protected WeldContainer(InstanceManager instanceManager, BeanManager beanManager)
   {
      this.instanceManager = instanceManager;
      this.beanManager = beanManager;
   }

   /**
    * Provides access to all beans within the application. For example:
    * <code>
    * Foo foo = weld.instance().select(Foo.class).get();
    * </code>
    */
   public Instance<Object> instance()
   {
      return instanceManager.getInstances();
   }

   /**
    * Provides access to all events within the application. For example:
    * <code>
    * weld.event().select(Bar.class).fire(new Bar());
    * </code>
    */
   public Event<Object> event()
   {
      return instanceManager.getEvents();
   }

   /**
    * Provides direct access to the BeanManager.
    */
   public BeanManager getBeanManager()
   {
      return beanManager;
   }
}
