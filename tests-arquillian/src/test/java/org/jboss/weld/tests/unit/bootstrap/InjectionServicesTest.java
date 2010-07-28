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

import java.util.Arrays;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.injection.spi.InjectionServices;
import org.jboss.weld.mock.MockEELifecycle;
import org.jboss.weld.mock.TestContainer;
import org.junit.Assert;
import org.junit.Test;

public class InjectionServicesTest
{
   
   @Test
   public void testInjectionOfTarget()
   {
      TestContainer container = new TestContainer(new MockEELifecycle(), Arrays.asList(Foo.class, Bar.class), null);
      CheckableInjectionServices ijs = new CheckableInjectionServices();
      container.getLifecycle().getWar().getServices().add(InjectionServices.class, ijs);
      container.startContainer();
      container.ensureRequestActive();
      
      BeanManager manager = container.getBeanManager();
      
      Bean<? extends Object> bean = manager.resolve(manager.getBeans(Foo.class));
      ijs.reset();
      Foo foo = (Foo) manager.getReference(bean, Foo.class, manager.createCreationalContext(bean));
      
      Assert.assertTrue(ijs.isBefore());
      Assert.assertTrue(ijs.isAfter());
      Assert.assertTrue(ijs.isInjectedAfter());
      Assert.assertTrue(ijs.isInjectionTargetCorrect());
      
      Assert.assertNotNull(foo.getBar());
      Assert.assertEquals("hi!", foo.getMessage());
      
      
      container.stopContainer();
   }
   
}
