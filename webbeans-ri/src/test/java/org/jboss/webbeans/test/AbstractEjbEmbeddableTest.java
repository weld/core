package org.jboss.webbeans.test;

import java.net.URL;
import java.util.Properties;

import javax.ejb.EJBContainer;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public abstract class AbstractEjbEmbeddableTest extends AbstractTest
{

   @AfterClass
   public void afterClass()
   {
      EJBContainer current = EJBContainer.getCurrentEJBContainer();
      if(current != null)
      {
         current.close();
      }
   }
   
   @BeforeClass
   public void beforeClass()
   {
      Properties properties = new Properties();
      String module = getURLToTestClasses(getTestClassesPath());
      properties.setProperty(EJBContainer.EMBEDDABLE_MODULES_PROPERTY, module);
      
      EJBContainer.createEJBContainer(properties);
   }
   
   protected abstract String getTestClassesPath();
   
   private static String getURLToTestClasses(String path)
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource(path);
      String s = url.toString();
      return s.substring(0, s.length() - path.length());
   }
   
}
