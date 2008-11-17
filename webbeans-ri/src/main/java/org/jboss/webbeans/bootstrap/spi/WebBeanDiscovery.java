package org.jboss.webbeans.bootstrap.spi;

import java.net.URL;
import java.util.Map;

/**
 * A container should implement this interface to allow the Web Beans RI to
 * discover the Web Beans to deploy
 * 
 * @author Pete Muir
 *
 */
public interface WebBeanDiscovery
{
   /**
    * @return A list of all classes in classpath archives with web-beans.xml files
    */
   public Iterable<Class<?>> discoverWebBeanClasses();
   
   /**
    * @return A list of all web-beans.xml files in the app classpath 
    */
   public Iterable<URL> discoverWebBeansXml();
   
   /**
    * @return A Map of EJB descriptors, keyed by the EJB bean class
    */
   public Map<Class<?>, EjbDescriptor<?>> discoverEjbs();
   
}
