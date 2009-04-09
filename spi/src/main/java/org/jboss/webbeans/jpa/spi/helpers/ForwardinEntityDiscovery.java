package org.jboss.webbeans.jpa.spi.helpers;

import java.util.Collection;

import org.jboss.webbeans.persistence.spi.EntityDiscovery;

public abstract class ForwardinEntityDiscovery implements EntityDiscovery
{
   
   protected abstract EntityDiscovery delegate();

   public Collection<Class<?>> discoverEntitiesFromAnnotations()
   {
      return delegate().discoverEntitiesFromAnnotations();
   }

   public Collection<Class<?>> discoverEntitiesFromPersistenceUnits()
   {
      return delegate().discoverEntitiesFromPersistenceUnits();
   }

   public Collection<Class<?>> discoverEntitiesFromXml()
   {
      return delegate().discoverEntitiesFromXml();
   }

}
