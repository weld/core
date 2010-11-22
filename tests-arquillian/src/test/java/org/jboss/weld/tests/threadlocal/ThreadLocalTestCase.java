package org.jboss.weld.tests.threadlocal;

import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.arquillian.container.weld.ee.embedded_1_1.mock.TestContainer;
import org.junit.Assert;
import org.junit.Test;

public class ThreadLocalTestCase
{
   @SuppressWarnings("unused")
   @Inject
   private Foo foo;
   
   @SuppressWarnings("unused")
   @Inject 
   private void someInjectionPointCausingException(Foo foo)
   {
      throw new RuntimeException();
   }

   @Test
   public void ensureNoThreadLocalLeak() throws Exception
   {
      TestContainer container = new TestContainer(Foo.class, ThreadLocalTestCase.class);
      container.startContainer();
      BeanManager manager = getBeanManager(container);

      Bean<? extends Object> testBean = manager.resolve(manager.getBeans(ThreadLocalTestCase.class));
      
      try
      {
         manager.getReference(
               testBean, 
               ThreadLocalTestCase.class, 
               manager.createCreationalContext(testBean));
      }
      catch (RuntimeException e) 
      {
         // Ignore, expected
      }

      container.stopContainer();
      verifyThreadLocals();
   }

   /**
    * Get the bean manager, assuming a flat deployment structure
    */
   public static BeanManager getBeanManager(TestContainer container)
   {
      return container.getBeanManager(container.getDeployment().getBeanDeploymentArchives().iterator().next());
   }

   private void verifyThreadLocals() throws Exception
   {
      Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");

      threadLocalsField.setAccessible(true);
      Field inheritableThreadLocalsField = Thread.class.getDeclaredField("inheritableThreadLocals");

      inheritableThreadLocalsField.setAccessible(true);

      Thread thread = Thread.currentThread();

      Class<?> tlmClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
      Field size = tlmClass.getDeclaredField("size");
      Field table = tlmClass.getDeclaredField("table");

      size.setAccessible(true);
      table.setAccessible(true);

      verifyThreadLocalValues(
            extractThreadLocalValues(
                  threadLocalsField.get(thread), table));
      
      verifyThreadLocalValues(
            extractThreadLocalValues(
                  inheritableThreadLocalsField.get(thread), table));
            
   }
   
   private void verifyThreadLocalValues(Map<Object, Object> values)
   {
      for(Map.Entry<Object, Object> entry : values.entrySet())
      {
         String keyName = String.valueOf(entry.getKey());
         if(keyName != null)
         {
            Assert.assertFalse(
                  "Verify found ThreadLocal variable key [" + keyName + "] does not belong to org.jboss, with value[" + entry.getValue() + "]", 
                  keyName.startsWith("org.jboss"));   
         }
      }
   }

   private Map<Object, Object> extractThreadLocalValues(Object map, Field internalTableField) throws NoSuchMethodException,
         IllegalAccessException, NoSuchFieldException, InvocationTargetException
   {
      Map<Object, Object> values = new HashMap<Object, Object>();
      if (map != null)
      {
         Method mapRemove = map.getClass().getDeclaredMethod("remove", new Class[]
         {ThreadLocal.class});

         mapRemove.setAccessible(true);
         Object[] table = (Object[]) (Object[]) internalTableField.get(map);
         if (table != null)
         {
            for (int j = 0; j < table.length; ++j)
            {
               if (table[j] != null)
               {
                  Object key = ((Reference<?>) table[j]).get();
                  Field valueField = table[j].getClass().getDeclaredField("value");

                  valueField.setAccessible(true);
                  Object value = valueField.get(table[j]);
                  values.put(key, value);
               }
            }
         }
      }
      return values;
   }
}
