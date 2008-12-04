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
   
   private Map<Class<?>, EjbDescriptor<?>> ejbs = new HashMap<Class<?>, EjbDescriptor<?>>();
   
   public MockWebBeanDiscovery(Set<Class<?>> webBeanClasses, Set<URL> webBeansXmlFiles, Map<Class<?>, EjbDescriptor<?>> ejbs)
   {
      super();
      this.webBeanClasses = webBeanClasses;
      this.webBeansXmlFiles = webBeansXmlFiles;
      this.ejbs = ejbs;
   }

   public Iterable<Class<?>> discoverWebBeanClasses()
   {
      return webBeanClasses;
   }

   public Map<String, EjbDescriptor<?>> discoverEjbs()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Iterable<URL> discoverWebBeansXml()
   {
      return webBeansXmlFiles;
   }
   
}
