package org.jboss.weld.test;

import org.jboss.testharness.impl.runner.servlet.ServletTestRunner;
import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.servlet.ServletHelper;

public class BeanManagerLocator
{
   
   public static BeanManagerLocator INSTANCE = new BeanManagerLocator();
   
   private BeanManagerLocator()
   {
      // TODO Auto-generated constructor stub
   }
   
   private BeanDeploymentArchive testArchive;
   
   public BeanManagerImpl locate()
   {
      if (ServletTestRunner.getCurrentServletContext() != null)
      {
         return ServletHelper.getModuleBeanManager(ServletTestRunner.getCurrentServletContext());
      }
      else if (getTestArchive() != null)
      {
         return Container.instance().beanDeploymentArchives().get(getTestArchive());
      }
      else
      {
         throw new IllegalStateException();
      }
   }
   
   private BeanDeploymentArchive getTestArchive()
   {
      if (testArchive == null)
      {
         try
         {
            testArchive = (BeanDeploymentArchive) Class.forName("org.jboss.weld.mock.MockBeanDeploymentArchive").newInstance();
         }
         catch (InstantiationException e)
         {
            throw new IllegalStateException(e);
         }
         catch (IllegalAccessException e)
         {
            throw new IllegalStateException(e);
         }
         catch (ClassNotFoundException e)
         {
            throw new IllegalStateException(e);
         }
      }
      return testArchive;
   }

}
