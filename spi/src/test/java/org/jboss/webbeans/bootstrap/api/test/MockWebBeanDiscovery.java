package org.jboss.webbeans.bootstrap.api.test;

import java.net.URL;

import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;

public class MockWebBeanDiscovery implements WebBeanDiscovery
{
   
   public Iterable<Class<?>> discoverWebBeanClasses()
   {
      return null;
   }
   
   public Iterable<URL> discoverWebBeansXml()
   {
      return null;
   }
   
}
