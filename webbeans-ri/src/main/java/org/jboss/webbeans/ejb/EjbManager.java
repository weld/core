package org.jboss.webbeans.ejb;

import java.util.HashMap;
import java.util.Map;

import javax.webbeans.manager.EnterpriseBeanLookup;

import org.jboss.webbeans.util.JNDI;


public class EjbManager
{
   
   private Map<Class<?>, EjbMetaData<?>> ejbMetaDataMap = new HashMap<Class<?>, EjbMetaData<?>>();
   
   private EnterpriseBeanLookup enterpriseBeanLookup;
   
   public EjbManager()
   {
      // TODO Write enterpriseBeanLookup instantiation
   }
   
   // TODO Should this be static?
   public <T> T lookup(EjbMetaData<T> ejb)
   {
      if (ejb.getEjbLinkJndiName() != null)
      {
         return (T) JNDI.lookup(ejb.getEjbLinkJndiName(), ejb.getType());
      }
      try
      {
         // TODO How is a JNDI lookup failure shown to us?
         return (T) JNDI.lookup(ejb.getDefaultJndiName(), ejb.getType());
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

}
