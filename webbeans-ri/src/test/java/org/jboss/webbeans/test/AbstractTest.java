package org.jboss.webbeans.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import javax.webbeans.Production;
import javax.webbeans.Standard;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.HornedAnimalDeploymentType;
import org.jboss.webbeans.test.mock.MockBootstrap;
import org.jboss.webbeans.test.mock.MockEjbDescriptor;
import org.testng.annotations.BeforeMethod;

public class AbstractTest
{
   protected static final int BUILT_IN_BEANS = 3;
   
   protected ManagerImpl manager;
   protected MockBootstrap webBeansBootstrap;

   public static boolean visited = false;

   @BeforeMethod
   public final void before()
   {
      webBeansBootstrap = new MockBootstrap();
      manager = webBeansBootstrap.getManager();
      addStandardDeploymentTypesForTests();
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
   protected void addStandardDeploymentTypesForTests()
   {
      manager.setEnabledDeploymentTypes(Arrays.asList(Standard.class, Production.class, AnotherDeploymentType.class, HornedAnimalDeploymentType.class));
   }

   protected <T> void addToEjbCache(Class<T> clazz)
   {
      manager.getEjbDescriptorCache().add(new MockEjbDescriptor<T>(clazz));
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
