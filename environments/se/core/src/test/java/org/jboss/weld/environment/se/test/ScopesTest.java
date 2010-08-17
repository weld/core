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


import static org.junit.Assert.assertEquals;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.environment.se.ShutdownManager;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.test.scopes.Bar;
import org.jboss.weld.environment.se.test.scopes.Foo;
import org.junit.Test;

/**
 * 
 * @author Peter Royle
 */
public class ScopesTest
{

   /**
    * Test that decorators work as expected in SE.
    */
   @Test
   // WELD-322
   public void testScopes()
   {

      WeldContainer weld = new Weld().initialize();
      BeanManager manager = weld.getBeanManager();

      assertEquals(1, manager.getBeans(Bar.class).size());
      assertEquals(2, manager.getBeans(Foo.class).size());

      shutdownManager(weld);
   }

   private void shutdownManager(WeldContainer weld)
   {
      ShutdownManager shutdownManager = weld.instance().select(ShutdownManager.class).get();
      shutdownManager.shutdown();
   }
}
