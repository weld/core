package org.jboss.webbeans.test.mock;

import java.util.HashSet;
import java.util.Set;

import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;

public class MockWebBeanDiscovery implements WebBeanDiscovery
{
   
   public static Set<Class<?>> webBeanClasses = new HashSet<Class<?>>();
   
   public Set<Class<?>> discoverWebBeanClasses()
   {
      return webBeanClasses;
   }
   
}
