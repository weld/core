package org.jboss.webbeans.test.mock;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.webbeans.bootstrap.spi.EjbDescriptor;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;

public class MockWebBeanDiscovery implements WebBeanDiscovery
{

   private Set<Class<?>> webBeanClasses = new HashSet<Class<?>>();

   private Set<URL> webBeansXmlFiles = new HashSet<URL>();

   private Map<String, EjbDescriptor<?>> ejbs = new HashMap<String, EjbDescriptor<?>>();

   @SuppressWarnings("unchecked")
   public MockWebBeanDiscovery(Set<Class<?>> webBeanClasses, Set<URL> webBeansXmlFiles, Set<Class<?>> ejbs)
   {
      super();
      this.webBeanClasses = webBeanClasses;
      this.webBeansXmlFiles = webBeansXmlFiles;
      this.ejbs = new HashMap<String, EjbDescriptor<?>>();
      for (Class<?> ejb : ejbs)
      {
         String ejbName = getEjbName(ejb);
         this.ejbs.put(ejbName, new MockEjbDescriptor(ejbName, ejb));
      }
   }

   private String getEjbName(Class<?> clazz)
   {
      return clazz.getSimpleName() + "/local";
   }

   public Iterable<Class<?>> discoverWebBeanClasses()
   {
      return webBeanClasses;
   }

   public Map<String, EjbDescriptor<?>> discoverEjbs()
   {
      // TODO Auto-generated method stub
      return new HashMap<String, EjbDescriptor<?>>();
   }

   public Iterable<URL> discoverWebBeansXml()
   {
      return webBeansXmlFiles;
   }

}
