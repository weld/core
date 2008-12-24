package org.jboss.webbeans.resources.spi;

import java.net.URL;


public interface ResourceLoader
{
   
   public static final String PROPERTY_NAME = ResourceLoader.class.getName();
   
   public Class<?> classForName(String name);
   
   public URL getResource(String name);
   
   public Iterable<URL> getResources(String name);
   
}
