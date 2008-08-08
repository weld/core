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
   public static final String WEBBEANS_CONTAINER_KEY = "javax.webbeans.Container";
   
   private ServletContext servletContext;
   
   DeploymentStrategy deploymentStrategy;
      
   public Initialization(ServletContext servletContext)
   {
      this.servletContext = servletContext;
   }
   
   public Initialization create()
   {
      Container container = new ContainerImpl(null);  
      
      servletContext.setAttribute(WEBBEANS_CONTAINER_KEY, container);
      
      deploymentStrategy = new DeploymentStrategy(Thread.currentThread().getContextClassLoader(), container);
      return this;
   }   
   
   public void init()
   {
      deploymentStrategy.scan();
   }
}
