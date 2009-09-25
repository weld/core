package org.jboss.webbeans.test.unit.cluster;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import javax.enterprise.inject.spi.Bean;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bootstrap.api.SingletonProvider;
import org.jboss.webbeans.context.ContextLifecycle;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.mock.MockEELifecycle;
import org.jboss.webbeans.mock.TestContainer;
import org.testng.annotations.Test;

public class NaiveClusterTest
{

   @Test
   public void testSessionReplication() throws Exception
   {
      SingletonProvider.initialize(new SwitchableSingletonProvider());
      
      // Bootstrap container 1
      SwitchableSingletonProvider.use(1);
      
      TestContainer container1 = new TestContainer(new MockEELifecycle(), Arrays.<Class<?>>asList(Foo.class), null);
      container1.startContainer();
      container1.ensureRequestActive();
      
      BeanManagerImpl beanManager1 = container1.getBeanManager();
      Bean<?> fooBean1 = beanManager1.resolve(beanManager1.getBeans(Foo.class));
      
      // Bootstrap container 2
      SwitchableSingletonProvider.use(2);
      
      TestContainer container2 = new TestContainer(new MockEELifecycle(), Arrays.<Class<?>>asList(Foo.class), null);
      container2.startContainer();
      container2.ensureRequestActive();
      
      BeanManagerImpl beanManager2 = container2.getBeanManager();
      Bean<?> fooBean2 = beanManager2.resolve(beanManager2.getBeans(Foo.class));
      
      SwitchableSingletonProvider.use(1);
      // Set a value into Foo1
      Foo foo1 = (Foo) beanManager1.getReference(fooBean1, Foo.class, beanManager1.createCreationalContext(fooBean1));
      foo1.setName("container 1");
      
      replicateSession(beanManager1, beanManager2);
      
      SwitchableSingletonProvider.use(2);
      Foo foo2 = (Foo) beanManager2.getReference(fooBean2, Foo.class, beanManager2.createCreationalContext(fooBean2));
      assert foo2.getName().equals("container 1");
   }
   
   private void replicateSession(BeanManagerImpl beanManager1, BeanManagerImpl beanManager2) throws Exception
   {
      // Mimic replicating the session
      BeanStore sessionBeanStore = beanManager1.getServices().get(ContextLifecycle.class).getSessionContext().getBeanStore();
      BeanStore replicatedSessionBeanStore = (BeanStore) deserialize(serialize(sessionBeanStore));
      beanManager2.getServices().get(ContextLifecycle.class).getSessionContext().setBeanStore(replicatedSessionBeanStore);
   }
   
   protected byte[] serialize(Object instance) throws IOException
   {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bytes);
      out.writeObject(instance);
      return bytes.toByteArray();
   }

   protected Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException
   {
      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
      return in.readObject();
   }
   
}
