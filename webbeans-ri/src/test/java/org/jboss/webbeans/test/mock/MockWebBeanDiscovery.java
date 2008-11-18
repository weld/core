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
   
   public static Set<Class<?>> webBeanClasses = new HashSet<Class<?>>();

   public static Set<URL> webBeansXmlFiles = new HashSet<URL>();
   
   public static Map<Class<?>, EjbDescriptor<?>> ejbs = new HashMap<Class<?>, EjbDescriptor<?>>();
   
   public Iterable<Class<?>> discoverWebBeanClasses()
   {
      return webBeanClasses;
   }

   public Map<Class<?>, EjbDescriptor<?>> discoverEjbs()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Iterable<URL> discoverWebBeansXml()
   {
      return webBeansXmlFiles;
   }
   
}
