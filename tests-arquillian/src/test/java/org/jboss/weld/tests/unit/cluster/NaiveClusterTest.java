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
package org.jboss.weld.tests.unit.cluster;

import java.util.Arrays;
import java.util.Collection;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.mock.TestContainer;
import org.jboss.weld.mock.cluster.AbstractClusterTest;
import org.junit.Assert;
import org.junit.Test;

public class NaiveClusterTest extends AbstractClusterTest
{
   /*
    * description = "A simple test to check session replication, doesn't carefully check if a bean ids are correct"
    */
   @Test
   public void testSimpleSessionReplication() throws Exception
   {
      
      TestContainer container1 = bootstrapContainer(1, Arrays.<Class<?>>asList(Foo.class));
      BeanManagerImpl beanManager1 = container1.getBeanManager();
      Bean<?> fooBean1 = beanManager1.resolve(beanManager1.getBeans(Foo.class));
      
      TestContainer container2 = bootstrapContainer(2, Arrays.<Class<?>>asList(Foo.class));
      BeanManagerImpl beanManager2 = container2.getBeanManager();
      Bean<?> fooBean2 = beanManager2.resolve(beanManager2.getBeans(Foo.class));
      
      use(1);
      // Set a value into Foo1
      Foo foo1 = (Foo) beanManager1.getReference(fooBean1, Foo.class, beanManager1.createCreationalContext(fooBean1));
      foo1.setName("container 1");
      
      replicateSession(1, beanManager1, 2, beanManager2);
      
      use(2);
      Foo foo2 = (Foo) beanManager2.getReference(fooBean2, Foo.class, beanManager2.createCreationalContext(fooBean2));
      Assert.assertEquals("container 1", foo2.getName());
      use(2);
      container2.stopContainer();
      use(1);
      container1.stopContainer();
   }
   
   @Test
   public void testMultipleDependentObjectsSessionReplication() throws Exception
   {
      Collection<Class<?>> classes = Arrays.<Class<?>>asList(Stable.class, Horse.class, Fodder.class);
      TestContainer container1 = bootstrapContainer(1, classes);
      BeanManagerImpl beanManager1 = container1.getBeanManager();
      Bean<?> stableBean1 = beanManager1.resolve(beanManager1.getBeans(Stable.class));
      
      TestContainer container2 = bootstrapContainer(2, classes);
      BeanManagerImpl beanManager2 = container2.getBeanManager();
      Bean<?> stableBean2 = beanManager2.resolve(beanManager2.getBeans(Stable.class));
      
      use(1);
      // Set a value into Foo1
      Stable stable1 = (Stable) beanManager1.getReference(stableBean1, Foo.class, beanManager1.createCreationalContext(stableBean1));
      stable1.getFodder().setAmount(10);
      stable1.getHorse().setName("George");
      
      replicateSession(1, beanManager1, 2, beanManager2);
      
      use(2);
      
      Stable stable2 = (Stable) beanManager2.getReference(stableBean2, Stable.class, beanManager2.createCreationalContext(stableBean2));
      Assert.assertEquals(stable2.getFodder().getAmount(), stable1.getFodder().getAmount());
      Assert.assertNull(stable2.getHorse().getName());
      
      use(1);
      Assert.assertEquals(10, stable1.getFodder().getAmount());
      Assert.assertEquals("George", stable1.getHorse().getName());
      
      use(2);
      
      stable2.getFodder().setAmount(11);
      
      replicateSession(2, beanManager2, 1, beanManager1);
      
      use(1);
      
      Assert.assertEquals(11, stable1.getFodder().getAmount());
      use(1);
      container1.stopContainer();
      use(2);
      container2.stopContainer();
   }
   
   @Test
   public void testVariableBeanDeploymentStructure() throws Exception
   {
      // NB This is not a valid deployment scenario for a cluster, but it does allow us to test bean ids neatly!
      Collection<Class<?>> classes1 = Arrays.<Class<?>>asList(Stable.class, Horse.class, Fodder.class);
      Collection<Class<?>> classes2 = Arrays.<Class<?>>asList(Stable.class, Horse.class, Fodder.class, Foo.class);
      TestContainer container1 = bootstrapContainer(1, classes1);
      BeanManagerImpl beanManager1 = container1.getBeanManager();
      Bean<?> stableBean1 = beanManager1.resolve(beanManager1.getBeans(Stable.class));
      
      TestContainer container2 = bootstrapContainer(2, classes2);
      BeanManagerImpl beanManager2 = container2.getBeanManager();
      Bean<?> stableBean2 = beanManager2.resolve(beanManager2.getBeans(Stable.class));
      
      use(1);
      // Set a value into Foo1
      Stable stable1 = (Stable) beanManager1.getReference(stableBean1, Foo.class, beanManager1.createCreationalContext(stableBean1));
      stable1.getFodder().setAmount(10);
      stable1.getHorse().setName("George");
      
      replicateSession(1, beanManager1, 2, beanManager2);
      
      use(2);
      
      Stable stable2 = (Stable) beanManager2.getReference(stableBean2, Stable.class, beanManager2.createCreationalContext(stableBean2));
      Assert.assertEquals(stable2.getFodder().getAmount(), stable1.getFodder().getAmount());
      Assert.assertNull(stable2.getHorse().getName());
      use(1);
      container1.stopContainer();
      use(2);
      container2.stopContainer();
   }
   
}
