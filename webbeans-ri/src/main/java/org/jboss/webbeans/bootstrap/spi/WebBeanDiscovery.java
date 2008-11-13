package org.jboss.webbeans.bootstrap.spi;

import java.util.Set;

public interface WebBeanDiscovery
{
   
   public Set<Class<?>> discoverWebBeanClasses();
   
}
