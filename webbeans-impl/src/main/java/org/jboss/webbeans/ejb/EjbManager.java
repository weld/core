package org.jboss.webbeans.ejb;

import java.util.HashMap;
import java.util.Map;

import javax.webbeans.EnterpriseBeanLookup;


public class EjbManager
{
   
   private Map<Class<?>, EjbMetaData<?>> ejbMetaDataMap = new HashMap<Class<?>, EjbMetaData<?>>();
   
   private EnterpriseBeanLookup enterpriseBeanLookup;
   
   public EjbManager()
   {
      // TODO Write enterpriseBeanLookup instantiation
   }
   
   // TODO Should this be static?
   @SuppressWarnings("unchecked")
   public <T> T lookup(EjbMetaData<T> ejb)
   {
      if (ejb.getEjbLinkJndiName() != null)
      {
         return (T) lookupInJndi(ejb.getEjbLinkJndiName());
      }
      try
      {
         // TODO How is a JNDI lookup failure shown to us?
         return (T) lookupInJndi(ejb.getDefaultJndiName());
      }
      catch (Exception e) 
      {
         
      }
      return (T) enterpriseBeanLookup.lookup(ejb.getEjbName());
   }
   
   @SuppressWarnings("unchecked")
   public <T> EjbMetaData<T> getEjbMetaData(Class<? extends T> clazz)
   {
      // TODO replace with an application lookup
      if (!ejbMetaDataMap.containsKey(clazz))
      {
         EjbMetaData<T> ejbMetaData = new EjbMetaData<T>(clazz); 
         ejbMetaDataMap.put(clazz, ejbMetaData);
         return ejbMetaData;
      }
      else
      {
         return (EjbMetaData<T>) ejbMetaDataMap.get(clazz);
      }
      
   }
   
   protected Object lookupInJndi(String name)
   {
      // TODO Write JNDI lookup
      return null;
   }

}
