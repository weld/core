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
package org.jboss.weld.environment.se.test;

import static org.junit.Assert.assertFalse;

import org.jboss.weld.environment.se.ShutdownManager;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.jboss.weld.environment.se.test.events.Foo;
import org.junit.Test;

/**
 * 
 * @author Peter Royle
 */
public class EventsTest
{

   // forum post check
   @Test
   public void testEventQualifiersCorrect()
   {
      Foo.reset();
      WeldContainer weld = new Weld().initialize();
      weld.event().select(ContainerInitialized.class).fire(new ContainerInitialized());
      assertFalse(Foo.isObservedEventTest());
      shutdownManager(weld);
   }

   private void shutdownManager(WeldContainer weld)
   {
      ShutdownManager shutdownManager = weld.instance().select(ShutdownManager.class).get();
      shutdownManager.shutdown();
   }
}
