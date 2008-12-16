package org.jboss.webbeans.ejb;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.webbeans.bootstrap.spi.EjbDescriptor;
import org.jboss.webbeans.test.mock.MockEjbDescriptor;
import org.jboss.webbeans.util.Strings;

import com.google.common.collect.ForwardingMap;

public class EjbDescriptorCache
{
   private static EjbDescriptorCache instance;

   public static EjbDescriptorCache instance()
   {
      return instance;
   }

   static
   {
      instance = new EjbDescriptorCache();
   }

   private class EjbDescriptorMap extends ForwardingMap<String, EjbDescriptor<?>>
   {
      private Map<String, EjbDescriptor<?>> delegate;

      public EjbDescriptorMap()
      {
         delegate = new ConcurrentHashMap<String, EjbDescriptor<?>>();
      }

      @Override
      protected Map<String, EjbDescriptor<?>> delegate()
      {
         return delegate;
      }

      @Override
      public String toString()
      {
         return Strings.mapToString("EjbMetaDataMap (EJB name -> metadata): ", delegate);
      }
   }

   private EjbDescriptorMap ejbDescriptorMap = new EjbDescriptorMap();

   public void setEjbDescriptors(Map<String, EjbDescriptor<?>> ejbDescriptorMap)
   {
      ejbDescriptorMap.putAll(ejbDescriptorMap);
   }

   public EjbDescriptor<?> get(String ejbName)
   {
      return ejbDescriptorMap.get(ejbName);
   }

   public void addEjbDescriptor(String ejbName, EjbDescriptor<?> ejbDescriptor)
   {
      ejbDescriptorMap.put(ejbName, ejbDescriptor);
   }

}
