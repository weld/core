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
package org.jboss.weld.environment.se.beans;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.weld.environment.se.WeldContainer;

/**
 * A managed bean which holds all of the injected instances of managed beans and
 * events. It is primarily used as a delegate for the {@link WeldContainer} class's instance()
 * and event() methods.
 * 
 * @see WeldContainer
 * @author Peter Royle
 */
public class InstanceManager
{

   @Inject Instance<Object> instances;
   @Inject Event<Object> events;

   public InstanceManager()
   {
   }

   public Instance<Object> getInstances()
   {
      return instances;
   }

   public Event<Object> getEvents()
   {
      return events;
   }
}
