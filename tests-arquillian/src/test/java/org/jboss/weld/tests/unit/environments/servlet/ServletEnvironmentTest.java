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
package org.jboss.weld.tests.unit.environments.servlet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.AnnotationLiteral;

import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.mock.MockServletLifecycle;
import org.jboss.weld.mock.TestContainer;
import org.jboss.weld.test.Utils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ServletEnvironmentTest
{
   
   private static TestContainer container;
   private static BeanManagerImpl manager;
   
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      container = new TestContainer(new MockServletLifecycle(), Arrays.asList(Animal.class, DeadlyAnimal.class, DeadlySpider.class, DeadlyAnimal.class, Hound.class, HoundLocal.class, Salmon.class, ScottishFish.class, SeaBass.class, Sole.class, Spider.class, Tarantula.class, TarantulaProducer.class, Tuna.class), null);
      container.startContainer();
      container.ensureRequestActive();
      manager = container.getBeanManager();
   }
   
   @AfterClass
   public static void afterClass() throws Exception
   {
      container.stopContainer();
      container = null;
      manager = null;
   }
   
   @Test
   public void testSimpleBeans()
   {
      Map<Class<?>, Bean<?>> beans = new HashMap<Class<?>, Bean<?>>();
      for (Bean<?> bean : manager.getBeans())
      {
         if (bean instanceof RIBean<?>)
         {
            beans.put(((RIBean<?>) bean).getType(), bean);
         }
      }
      Assert.assertTrue(beans.containsKey(Tuna.class));
      Assert.assertTrue(beans.containsKey(Salmon.class));
      Assert.assertTrue(beans.containsKey(SeaBass.class));
      Assert.assertTrue(beans.containsKey(Sole.class));
      
      Assert.assertTrue(beans.get(Tuna.class) instanceof ManagedBean<?>);
      Assert.assertTrue(beans.get(Salmon.class) instanceof ManagedBean<?>);
      Assert.assertTrue(beans.get(SeaBass.class) instanceof ManagedBean<?>);
      Assert.assertTrue(beans.get(Sole.class) instanceof ManagedBean<?>);
      Utils.getReference(manager, Sole.class, new AnnotationLiteral<Whitefish>() {}).ping();
   }
   
   @Test
   public void testProducerMethodBean()
   {
      Map<Class<?>, Bean<?>> beans = new HashMap<Class<?>, Bean<?>>();
      for (Bean<?> bean : manager.getBeans())
      {
         if (bean instanceof RIBean<?>)
         {
            beans.put(((RIBean<?>) bean).getType(), bean);
         }
      }
      Assert.assertTrue(beans.containsKey(TarantulaProducer.class));
      Assert.assertTrue(beans.containsKey(Tarantula.class));
      
      beans.get(TarantulaProducer.class);
      
      Assert.assertTrue(beans.get(TarantulaProducer.class) instanceof ManagedBean<?>);
      Utils.getReference(manager, Tarantula.class, new AnnotationLiteral<Tame>() {}).ping();
   }
   
   public void testSingleEnterpriseBean()
   {
      List<Bean<?>> beans = manager.getBeans();
      Map<Class<?>, Bean<?>> classes = new HashMap<Class<?>, Bean<?>>();
      for (Bean<?> bean : beans)
      {
         if (bean instanceof RIBean<?>)
         {
            classes.put(((RIBean<?>) bean).getType(), bean);
         }
      }
      Assert.assertTrue(classes.containsKey(Hound.class));
      Assert.assertTrue(classes.get(Hound.class) instanceof ManagedBean<?>);
      Utils.getReference(manager, HoundLocal.class, new AnnotationLiteral<Tame>() {}).ping();
   }
   
}
