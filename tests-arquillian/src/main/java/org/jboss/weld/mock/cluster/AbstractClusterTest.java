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
package org.jboss.weld.mock.cluster;

import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.api.Singleton;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.context.beanstore.HashMapBeanStore;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.mock.TestContainer;
import org.jboss.weld.serialization.spi.ProxyServices;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class AbstractClusterTest
{

   private static Singleton<Container> singleton;

   @SuppressWarnings("unchecked")
   @BeforeClass
   public static void beforeClass() throws Exception
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
   public static void afterClass() throws Exception
   {
      getInstanceField().set(null, singleton);
   }

   protected TestContainer bootstrapContainer(int id, Collection<Class<?>> classes)
   {
      // Bootstrap container
      SwitchableSingletonProvider.use(id);

      TestContainer container = new TestContainer(new SwitchableMockEELifecycle(), classes, null);
      container.startContainer();
      container.ensureRequestActive();

      return container;
   }

   protected void use(int id)
   {
      SwitchableSingletonProvider.use(id);
   }

   private Map<String, ContextualInstance<?>> getContextualInstances(BeanStore beanStore)
   {
      Map<String, ContextualInstance<?>> instances = new HashMap<String, ContextualInstance<?>>();
      for (String id : beanStore.getContextualIds())
      {
         instances.put(id, beanStore.get(id));
      }
      return instances;
   }

   private BeanStore setContextualInstances(Map<String, ContextualInstance<?>> instances)
   {
      BeanStore beanStore = new HashMapBeanStore();
      for (Map.Entry<String, ContextualInstance<?>> i : instances.entrySet())
      {
         beanStore.put(i.getKey(), i.getValue());
      }
      return beanStore;
   }

   @SuppressWarnings("unchecked")
   protected void replicateSession(int fromId, BeanManagerImpl fromBeanManager, int toId, BeanManagerImpl toBeanManager) throws Exception
   {
      // Mimic replicating the session
      BeanStore sessionBeanStore = fromBeanManager.getServices().get(ContextLifecycle.class).getSessionContext().getBeanStore();
      Map<String, ContextualInstance<?>> contextuals = getContextualInstances(sessionBeanStore);
      byte[] bytes = serialize(contextuals);
      use(toId);
      BeanStore replicatedSessionBeanStore = setContextualInstances((Map<String, ContextualInstance<?>>) deserialize(bytes));
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

   protected void useNewClassLoader(ClassLoader parentClassLoader)
   {
      ((SwitchableCLProxyServices)Container.instance().services().get(ProxyServices.class)).useNewClassLoader(parentClassLoader);
   }
}