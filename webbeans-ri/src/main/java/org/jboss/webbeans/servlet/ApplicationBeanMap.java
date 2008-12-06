/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webbeans.servlet;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletContext;
import javax.webbeans.manager.Contextual;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.contexts.AbstractBeanMapAdaptor;
import org.jboss.webbeans.contexts.ApplicationContext;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

/**
 * Abstracts the servlet API specific application context
 * as a Map.
 * 
 * @author Gavin King
 */
public class ApplicationBeanMap extends AbstractBeanMapAdaptor
{
   private LogProvider log = Logging.getLogProvider(ApplicationBeanMap.class);
   
   // The current servlet context
   private ServletContext servletContext;

   /**
    * Constructor
    * 
    * @param servletContext The servlet context
    */
   public ApplicationBeanMap(ServletContext servletContext)
   {
      this.servletContext = servletContext;
   }

   
   public void clear()
   {
      throw new UnsupportedOperationException(); 
   }

   @SuppressWarnings("unchecked")
   public <T> T get(Contextual<? extends T> bean)
   {
      String key = getBeanKey(bean);
      T instance = (T) servletContext.getAttribute(key);
      log.trace("Searched application for key " + key + " and got " + instance);
      return instance; 
   }

   public <T> void put(Contextual<? extends T> bean, T instance)
   {
      String key = getBeanKey(bean);
      servletContext.setAttribute(key, instance);
      log.trace("Stored instance " + instance + " for bean " + bean + " under key " + key + " in session");
   }

   @SuppressWarnings("unchecked")
   public <T> T remove(Contextual<? extends T> bean)
   {
      String key = getBeanKey(bean);
      T result = (T) servletContext.getAttribute(key);
      servletContext.removeAttribute(key);
      return result;
   }

   @SuppressWarnings("unchecked")
   public Iterable<Contextual<? extends Object>> keySet()
   {
      List<Contextual<?>> beans = new ArrayList<Contextual<?>>();

      Enumeration names = servletContext.getAttributeNames();
      while (names.hasMoreElements())
      {
         String name = (String) names.nextElement();
         if (name.startsWith(getKeyPrefix()))
         {
            String id = name.substring(getKeyPrefix().length());
            Contextual<?> bean = CurrentManager.rootManager().getBeans().get(Integer.parseInt(id));
            beans.add(bean);
         }
      }

      return beans;
   }

   @Override
   protected String getKeyPrefix()
   {
      return ApplicationContext.class.getName();
   }

}
