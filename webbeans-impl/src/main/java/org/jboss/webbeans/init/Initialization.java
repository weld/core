package org.jboss.webbeans.init;

import javax.servlet.ServletContext;
import javax.webbeans.Container;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.deployment.DeploymentStrategy;

/**
 * Initializes the WebBeans metadata
 * 
 * @author Shane Bryzak
 *
 */
public class Initialization
{
   private ServletContext servletContext;
   
   DeploymentStrategy deploymentStrategy;
   
   private static Container container;
   
   public Initialization(ServletContext servletContext)
   {
      this.servletContext = servletContext;
   }
   
   public Initialization create()
   {
      container = new ContainerImpl(null);           
      deploymentStrategy = new DeploymentStrategy(Thread.currentThread().getContextClassLoader(), container);
      return this;
   }   
   
   public void init()
   {
      
   }
}
