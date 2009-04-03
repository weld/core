package org.jboss.webbeans.mock;

import java.net.URL;

import org.jboss.webbeans.bootstrap.api.ServiceRegistry;
import org.jboss.webbeans.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.webbeans.ejb.EjbDescriptorCache;
import org.jboss.webbeans.resources.ClassTransformer;
import org.jboss.webbeans.resources.DefaultResourceLoader;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.xml.XmlEnvironment;

public class MockXmlEnvironment extends XmlEnvironment
{

   private static final ServiceRegistry services;
   
   static
   {
      services = new SimpleServiceRegistry();
      services.add(ResourceLoader.class, new DefaultResourceLoader());
      services.add(ClassTransformer.class, new ClassTransformer());
   }
   
   public MockXmlEnvironment(Iterable<URL> beansXmlUrls, EjbDescriptorCache ejbDescriptors)
   {
      super(services, beansXmlUrls, ejbDescriptors);
   }
   
}
