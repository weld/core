package org.jboss.webbeans.test.unit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.webbeans.Production;
import javax.webbeans.Standard;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.AbstractProducerBean;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.ProducerFieldBean;
import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.test.mock.MockBootstrap;
import org.jboss.webbeans.test.mock.MockEjbDescriptor;
import org.testng.annotations.BeforeMethod;

public class AbstractTest
{
   
   protected abstract static class RunInDependentContext 
   {
      
      protected void setup()
      {
         AbstractTest.activateDependentContext();
      }
      
      protected void cleanup()
      {
         AbstractTest.deactivateDependentContext();
      }
      
      public final void run() throws Exception
      {
         try
         {
            setup();
            execute();
         }
         finally
         {
            cleanup();
         }
      }
      
      protected abstract void execute() throws Exception;
      
   }
   
   protected static final int BUILT_IN_BEANS = 3;
   
   protected ManagerImpl manager;
   protected MockBootstrap webBeansBootstrap;

   public static boolean visited = false;

   @BeforeMethod
   public void before() throws Exception
   {
      webBeansBootstrap = new MockBootstrap();
      manager = webBeansBootstrap.getManager();
      manager.setEnabledDeploymentTypes(getEnabledDeploymentTypes());
   }

   private boolean hasField(Class<?> clazz, String name)
   {
      try
      {
         Field field = clazz.getDeclaredField(name);
      }
      catch (NoSuchFieldException e)
      {
         return false;
      }
      return true;
   }
   
   private Method getMethod(Class<?> clazz, String name)
   {
      for (Method method : clazz.getDeclaredMethods())
      {
         if (method.getName().equals(name))
         {
            return method;
         }
      }
      return null;
   }
   
   protected AbstractProducerBean<?, ?> registerProducerBean(Class<?> producerBeanClass, String fieldOrMethodName, Class<?> productClass)
   {
      SimpleBean<?> producerContainerBean = SimpleBean.of(producerBeanClass, manager);
      manager.addBean(producerContainerBean);
      AbstractProducerBean<?, ?> producerBean = null;
      try
      {
         if (hasField(producerBeanClass, fieldOrMethodName))
         {
            Field producerField = producerBeanClass.getDeclaredField(fieldOrMethodName);
            producerBean = ProducerFieldBean.of(producerField, producerContainerBean, manager);
         }
         else
         {
            Method producerMethod = getMethod(producerBeanClass, fieldOrMethodName);
            producerBean = ProducerMethodBean.of(producerMethod, producerContainerBean, manager);
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not initialize producer bean", e);
      }
      manager.addBean(producerBean);
      return producerBean;
   }
   
   
   protected <T> AbstractClassBean<T> registerBean(Class<T> clazz)
   {
      AbstractClassBean<T> bean = null;
      if (CurrentManager.rootManager().getEjbDescriptorCache().containsKey(clazz))
      {
         bean = EnterpriseBean.of(clazz, manager);
      }
      else
      {
         bean = SimpleBean.of(clazz, manager);
      }
      CurrentManager.rootManager().addBean(bean);
      return bean;
   }

   protected void registerBeans(Class<?>[] classes)
   {
      for (Class<?> clazz : classes)
      {
         registerBean(clazz);
      }
   }

   @SuppressWarnings("unchecked")
   protected List<Class<? extends Annotation>> getEnabledDeploymentTypes()
   {
      return getDefaultDeploymentTypes();
   }
   
   protected final List<Class<? extends Annotation>> getDefaultDeploymentTypes()
   {
      return Arrays.asList(Standard.class, Production.class);
   }

   protected <T> void addToEjbCache(Class<T> clazz)
   {
      manager.getEjbDescriptorCache().add(MockEjbDescriptor.of(clazz));
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

   protected static void activateDependentContext()
   {
      DependentContext.INSTANCE.setActive(true);
   }
   
   protected static void deactivateDependentContext()
   {
      DependentContext.INSTANCE.setActive(false);
   }
}
