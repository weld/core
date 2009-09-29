package org.jboss.webbeans.mock.cluster;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bootstrap.api.SingletonProvider;
import org.jboss.webbeans.context.ContextLifecycle;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.mock.MockEELifecycle;
import org.jboss.webbeans.mock.TestContainer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class AbstractClusterTest
{

   @BeforeClass
   public void beforeClass()
   {
      SingletonProvider.reset();
      SingletonProvider.initialize(new SwitchableSingletonProvider());
   }
   
   @AfterClass
   public void afterClass()
   {
      SingletonProvider.reset();
   }

   protected TestContainer<MockEELifecycle> bootstrapContainer(int id, Iterable<Class<?>> classes)
   {
      // Bootstrap container
      SwitchableSingletonProvider.use(id);
      
      TestContainer<MockEELifecycle> container = new TestContainer<MockEELifecycle>(new MockEELifecycle(), classes, null);
      container.startContainer();
      container.ensureRequestActive();
      
      return container;
   }
   
   protected void use(int id)
   {
      SwitchableSingletonProvider.use(id);
   }

   protected void replicateSession(int fromId, BeanManagerImpl fromBeanManager, int toId, BeanManagerImpl toBeanManager) throws Exception
   {
      // Mimic replicating the session
      BeanStore sessionBeanStore = fromBeanManager.getServices().get(ContextLifecycle.class).getSessionContext().getBeanStore();
      byte[] bytes = serialize(sessionBeanStore);
      use(toId);
      BeanStore replicatedSessionBeanStore = (BeanStore) deserialize(bytes);
      toBeanManager.getServices().get(ContextLifecycle.class).getSessionContext().setBeanStore(replicatedSessionBeanStore);
      use(fromId);
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