package org.jboss.webbeans.tck;

import javax.enterprise.inject.UnproxyableResolutionException;
import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletContext;

import org.jboss.jsr299.tck.spi.Managers;
import org.jboss.testharness.impl.runner.servlet.ServletTestRunner;
import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.DeploymentException;
import org.jboss.webbeans.mock.MockServletContext;
import org.jboss.webbeans.servlet.ServletHelper;

public class ManagersImpl implements Managers
{
   
   private static final ServletContext SERVLET_CONTEXT = new MockServletContext("");

   public BeanManager getManager()
   {
      if (ServletTestRunner.getCurrentServletContext() != null)
      {
         return ServletHelper.getModuleBeanManager(ServletTestRunner.getCurrentServletContext());
      }
      else
      {
         return ServletHelper.getModuleBeanManager(SERVLET_CONTEXT);
      }
   }

   public boolean isDefinitionError(org.jboss.testharness.api.DeploymentException deploymentException)
   {
      return isDefinitionException(deploymentException.getCause());
   }

   private boolean isDefinitionException(Throwable t)
   {
      if (t == null)
      {
         return false;
      }
      else if (DefinitionException.class.isAssignableFrom(t.getClass()))
      {
         return true;
      }
      else
      {
         return isDefinitionException(t.getCause());
      }
   }

   public boolean isDeploymentError(org.jboss.testharness.api.DeploymentException deploymentException)
   {
      return isDeploymentException(deploymentException.getCause());
   }

   public boolean isDeploymentException(Throwable t)
   {
      if (t == null)
      {
         return false;
      }
      else if (DeploymentException.class.isAssignableFrom(t.getClass()))
      {
         return true;
      }
      else if (UnproxyableResolutionException.class.isAssignableFrom(t.getClass()))
      {
         return true;
      }
      else
      {
         return isDeploymentException(t.getCause());
      }
   }
}
