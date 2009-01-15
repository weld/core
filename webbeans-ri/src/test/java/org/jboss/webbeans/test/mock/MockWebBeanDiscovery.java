package org.jboss.webbeans.test.mock;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;

public class MockWebBeanDiscovery implements WebBeanDiscovery
{

   private Iterable<Class<?>> webBeanClasses = new HashSet<Class<?>>();

   private Iterable<URL> webBeansXmlFiles = new HashSet<URL>();

   /**
    * Simple constructor that auto discovers EJBs
    * @param webBeanClasses
    */
   public MockWebBeanDiscovery(Class<?>... webBeanClasses)
   {
      this(Arrays.asList(webBeanClasses), null);
   }
   
   public MockWebBeanDiscovery(Iterable<Class<?>> webBeanClasses, Iterable<URL> webBeansXmlFiles)
   {
      super();
      this.webBeanClasses = webBeanClasses;
      this.webBeansXmlFiles = webBeansXmlFiles;
   }

   public Iterable<Class<?>> discoverWebBeanClasses()
   {
      return webBeanClasses;
   }

   public Iterable<URL> discoverWebBeansXml()
   {
      return webBeansXmlFiles;
   }

}
