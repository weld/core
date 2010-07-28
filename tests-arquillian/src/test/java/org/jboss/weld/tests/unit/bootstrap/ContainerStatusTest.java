/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.unit.bootstrap;

import org.jboss.weld.Container;
import org.jboss.weld.ContainerState;
import org.jboss.weld.mock.MockServletLifecycle;
import org.jboss.weld.mock.TestContainer;
import org.junit.Assert;
import org.junit.Test;

public class ContainerStatusTest
{
   
   @Test
   public void testStatus()
   {
      TestContainer container = new TestContainer(new MockServletLifecycle());
      Assert.assertFalse(Container.available());
      container.getLifecycle().initialize();
      Assert.assertFalse(Container.available());
      Assert.assertEquals(ContainerState.STARTING, Container.instance().getState());
      container.getLifecycle().getBootstrap().startInitialization();
      Assert.assertFalse(Container.available());
      Assert.assertEquals(ContainerState.STARTING, Container.instance().getState());
      container.getLifecycle().getBootstrap().deployBeans();
      Assert.assertTrue(Container.available());
      Assert.assertEquals(ContainerState.INITIALIZED, Container.instance().getState());
      container.getLifecycle().getBootstrap().validateBeans().endInitialization();
      Assert.assertTrue(Container.available());
      Assert.assertEquals(ContainerState.VALIDATED, Container.instance().getState());
      container.stopContainer();
      Assert.assertFalse(Container.available());
   }

}
