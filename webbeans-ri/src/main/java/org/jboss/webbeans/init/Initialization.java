package org.jboss.webbeans.init;

import javax.servlet.ServletContext;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.deployment.DeploymentStrategy;

/**
 * Initializes the WebBeans metadata
 * 
 * @author Shane Bryzak
 *
 */
public class Initialization
{
   public static final String WEBBEANS_CONTAINER_KEY = "javax.webbeans.Container";
   
   private ServletContext servletContext;
   
   DeploymentStrategy deploymentStrategy;
      
   public Initialization(ServletContext servletContext)
   {
      this.servletContext = servletContext;
   }
   
   public Initialization create()
   {
      ManagerImpl container = new ManagerImpl(null);  
      
      servletContext.setAttribute(WEBBEANS_CONTAINER_KEY, container);
      
      deploymentStrategy = new DeploymentStrategy(Thread.currentThread().getContextClassLoader(), container);
      return this;
   }   
   
   public void init()
   {
      deploymentStrategy.scan();
   }
}
