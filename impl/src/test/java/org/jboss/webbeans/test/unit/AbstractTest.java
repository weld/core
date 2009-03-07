package org.jboss.webbeans.test.unit;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.inject.Production;
import javax.inject.Standard;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.mock.MockLifecycle;
import org.jboss.webbeans.mock.MockWebBeanDiscovery;
import org.jboss.webbeans.util.EnumerationIterable;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class AbstractTest
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
   protected MockLifecycle lifecycle;
   protected MockWebBeanDiscovery discovery;

   public static boolean visited = false;

   @BeforeMethod
   public void before() throws Exception
   {
      lifecycle = new MockLifecycle();
      this.discovery = lifecycle.getWebBeanDiscovery();
      this.manager = lifecycle.getBootstrap().getManager();
      lifecycle.beginApplication();
      lifecycle.beginSession();
      lifecycle.beginRequest();
   }
   
   @AfterMethod
   public void after() throws Exception
   {
      lifecycle.endRequest();
      lifecycle.endSession();
      lifecycle.endApplication();
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
   
   protected void deployBeans(Class<?>... classes)
   {
      discovery.setWebBeanClasses(Arrays.asList(classes));
      lifecycle.beginApplication();
      lifecycle.beginSession();
      lifecycle.beginRequest();
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
}
