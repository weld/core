package org.jboss.weld.mock.cluster;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Collection;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.api.Singleton;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.mock.MockEELifecycle;
import org.jboss.weld.mock.TestContainer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class AbstractClusterTest
{

   private Singleton<Container> singleton;
   
   @BeforeClass
   public void beforeClass() throws Exception
   {
      singleton = (Singleton) getInstanceField().get(null);
      getInstanceField().set(null, new SwitchableSingletonProvider().create(Container.class));
   }
   
   private static Field getInstanceField() throws Exception
   {
      Field field = Container.class.getDeclaredField("instance");
      field.setAccessible(true);
      return field;
   }
   
   @AfterClass
   public void afterClass() throws Exception
   {
      getInstanceField().set(null, singleton);
   }

   protected TestContainer bootstrapContainer(int id, Collection<Class<?>> classes)
   {
      // Bootstrap container
      SwitchableSingletonProvider.use(id);
      
      TestContainer container = new TestContainer(new MockEELifecycle(), classes, null);
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