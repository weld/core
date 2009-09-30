package org.jboss.webbeans.test.unit.cluster;

import java.util.Arrays;
import java.util.Collection;

import javax.enterprise.inject.spi.Bean;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.mock.TestContainer;
import org.jboss.webbeans.mock.cluster.AbstractClusterTest;
import org.testng.annotations.Test;

public class NaiveClusterTest extends AbstractClusterTest
{
   
   @Test(description="A simple test to check session replication, doesn't carefully check if a bean ids are correct")
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
      assert foo2.getName().equals("container 1");
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
      assert stable2.getFodder().getAmount() == stable1.getFodder().getAmount();
      assert stable2.getHorse().getName() == null;
      
      use(1);
      assert stable1.getFodder().getAmount() == 10;
      assert stable1.getHorse().getName().equals("George");
      
      use(2);
      
      stable2.getFodder().setAmount(11);
      
      replicateSession(2, beanManager2, 1, beanManager1);
      
      use(1);
      
      assert stable1.getFodder().getAmount() == 11;
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
      assert stable2.getFodder().getAmount() == stable1.getFodder().getAmount();
      assert stable2.getHorse().getName() == null;
   }
   
}
