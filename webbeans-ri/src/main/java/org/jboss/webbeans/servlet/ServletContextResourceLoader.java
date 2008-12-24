package org.jboss.webbeans.servlet;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.resources.spi.ResourceLoadingException;
import org.jboss.webbeans.util.EnumerationIterable;

public class ServletContextResourceLoader implements ResourceLoader
{
   
   private final ServletContext servletContext;
   
   public ServletContextResourceLoader(ServletContext servletContext)
   {
      this.servletContext = servletContext;
   }

   public Class<?> classForName(String name)
   {
      try
      {
         return servletContext.getClass().getClassLoader().loadClass(name);
      }
      catch (ClassNotFoundException e)
      {
         throw new ResourceLoadingException(e);
      }
      catch (NoClassDefFoundError e)
      {
         throw new ResourceLoadingException(e);
      }
   }
   
   public URL getResource(String name)
   {
      URL resource = getResourceFromServletContext(name);
      if (resource == null)
      {
         String stripped = name.startsWith("/") ? name.substring(1) : name;
         resource = servletContext.getClass().getClassLoader().getResource(name);
      }
      return resource;
   }
   
   private URL getResourceFromServletContext(String name)
   {
      try
      {
         return servletContext.getResource(name);
      }
      catch (Exception e) 
      {
         return null;
      }
   }
   
   public Iterable<URL> getResources(String name)
   {
      try
      {
         // TODO load resources from servlet context!
         return new EnumerationIterable<URL>(servletContext.getClass().getClassLoader().getResources(name));
      }
      catch (IOException e)
      {
         throw new ResourceLoadingException("Error loading resources for " + name, e);
      }
   }
   
}
