package org.jboss.webbeans.test;

import java.net.URL;
import java.util.Properties;

import javax.ejb.EJBContainer;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;



public abstract class AbstractEjbEmbeddableTest extends AbstractTest
{
   
   private EJBContainer ejbContainer;

   @AfterSuite
   public void afterClass() throws Exception
   {
      EJBContainer current = EJBContainer.getCurrentEJBContainer();
      if(current != null)
      {
         current.close();
      }
   }
   
   @BeforeSuite
   public void beforeClass() throws Exception
   {
      Properties properties = new Properties();
      String module = getURLToTestClasses(getTestClassesPath());
      properties.setProperty(EJBContainer.EMBEDDABLE_MODULES_PROPERTY, module);
      this.ejbContainer = EJBContainer.createEJBContainer(properties);
   }
   
   @BeforeMethod
   public void before() throws Exception
   {
      super.before();
      webBeansBootstrap.getNaming().setContext(new InitialContext());
   }
   
   @AfterMethod
   public void after() throws Exception
   {
      webBeansBootstrap.getNaming().setContext(null);
   }
   
   
   protected String getTestClassesPath()
   {
      return "org/jboss/webbeans/test";
   }
   
   private static String getURLToTestClasses(String path)
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource(path);
      String s = url.toString();
      return s.substring(0, s.length() - path.length());
   }
   
   public Context getContext()
   {
      return webBeansBootstrap.getNaming().getContext();
   }
   
}
