package org.jboss.webbeans.bootstrap.api.test;

import java.util.Collection;

import org.jboss.webbeans.persistence.spi.EntityDiscovery;

public class MockEntityDiscovery implements EntityDiscovery
{

   public Collection<Class<?>> discoverEntitiesFromAnnotations()
   {
      return null;
   }

   public Collection<Class<?>> discoverEntitiesFromPersistenceUnits()
   {
      return null;
   }

   public Collection<Class<?>> discoverEntitiesFromXml()
   {
      return null;
   }

}
