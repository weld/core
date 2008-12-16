package org.jboss.webbeans.test.mock;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.MessageDriven;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.Stateless;

import org.jboss.webbeans.bootstrap.spi.EjbDescriptor;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;

public class MockWebBeanDiscovery implements WebBeanDiscovery
{

   private Iterable<Class<?>> webBeanClasses = new HashSet<Class<?>>();

   private Iterable<URL> webBeansXmlFiles = new HashSet<URL>();

   private List<EjbDescriptor<?>> ejbs = new ArrayList<EjbDescriptor<?>>();

   /**
    * Simple constructor that auto discovers EJBs
    * @param webBeanClasses
    */
   public MockWebBeanDiscovery(Class<?>... webBeanClasses)
   {
      this(Arrays.asList(webBeanClasses));
   }
   
   public MockWebBeanDiscovery(Iterable<Class<?>> webBeanClasses)
   {
      this(webBeanClasses, null, discoverEjbs(webBeanClasses));
   }
   
   @SuppressWarnings("unchecked")
   public MockWebBeanDiscovery(Iterable<Class<?>> webBeanClasses, Iterable<URL> webBeansXmlFiles, Iterable<Class<?>> ejbs)
   {
      super();
      this.webBeanClasses = webBeanClasses;
      this.webBeansXmlFiles = webBeansXmlFiles;
      this.ejbs = new ArrayList<EjbDescriptor<?>>();
      for (Class<?> ejbClass : ejbs)
      {
         this.ejbs.add(new MockEjbDescriptor(ejbClass));
      }
   }

   public Iterable<Class<?>> discoverWebBeanClasses()
   {
      return webBeanClasses;
   }

   public Iterable<EjbDescriptor<?>> discoverEjbs()
   {
      return ejbs;
   }

   public Iterable<URL> discoverWebBeansXml()
   {
      return webBeansXmlFiles;
   }
   
   protected static Iterable<Class<?>> discoverEjbs(Iterable<Class<?>> webBeanClasses)
   {
      Set<Class<?>> ejbs = new HashSet<Class<?>>();
      for (Class<?> clazz : webBeanClasses)
      {
         if (clazz.isAnnotationPresent(Stateless.class) || clazz.isAnnotationPresent(Stateful.class) || clazz.isAnnotationPresent(MessageDriven.class) || clazz.isAnnotationPresent(Singleton.class)) 
         {
            ejbs.add(clazz);
         }
      }
      return ejbs;
   }

}
