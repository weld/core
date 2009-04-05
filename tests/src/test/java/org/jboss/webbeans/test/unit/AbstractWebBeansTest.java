package org.jboss.webbeans.test.unit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.inject.Production;
import javax.inject.Standard;

import org.jboss.testharness.AbstractTest;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.util.EnumerationIterable;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

public abstract class AbstractWebBeansTest extends AbstractTest
{
   
   protected abstract static class RunInDependentContext 
   {
      
      protected void setup()
      {
         DependentContext.INSTANCE.setActive(true);
      }
      
      protected void cleanup()
      {
         DependentContext.INSTANCE.setActive(false);
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

   public static boolean visited = false;
   
   @Override
   @BeforeSuite
   public void beforeSuite(ITestContext context) throws Exception
   {
      if (!isInContainer())
      {
         getCurrentConfiguration().setStandaloneContainers(new StandaloneContainersImpl());
         getCurrentConfiguration().getExtraPackages().add(AbstractWebBeansTest.class.getPackage().getName());
      }
      super.beforeSuite(context);
   }

   @BeforeMethod
   public void before() throws Exception
   {
      this.manager = CurrentManager.rootManager();
   }
   
   @AfterMethod
   public void after() throws Exception
   {
      manager = null;
   }
   

   protected List<Class<? extends Annotation>> getEnabledDeploymentTypes()
   {
      return getDefaultDeploymentTypes();
   }
   
   @SuppressWarnings("unchecked")
   protected final List<Class<? extends Annotation>> getDefaultDeploymentTypes()
   {
      return Arrays.asList(Standard.class, Production.class);
   }
   
   protected Iterable<URL> getResources(String name)
   {
      if (name.startsWith("/"))
      {
         name = name.substring(1);
      }
      else
      {
         name = getClass().getPackage().getName().replace(".", "/") + "/" + name;
      }
      try
      {
         return new EnumerationIterable<URL>(getClass().getClassLoader().getResources(name));
      }
      catch (IOException e)
      {
         throw new RuntimeException("Error loading resource from classloader" + name, e);
      }
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
