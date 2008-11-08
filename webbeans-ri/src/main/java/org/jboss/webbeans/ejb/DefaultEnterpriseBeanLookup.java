package org.jboss.webbeans.ejb;

import java.util.HashMap;
import java.util.Map;

import javax.webbeans.CreationException;
import javax.webbeans.Standard;
import javax.webbeans.manager.EnterpriseBeanLookup;

import org.jboss.webbeans.util.JNDI;

@Standard
public class DefaultEnterpriseBeanLookup implements EnterpriseBeanLookup
{
   public Object lookup(String ejbName)
   {
      // TODO Auto-generated method stub
      return null;
   }
   private Map<String, EjbMetaData<?>> ejbMetaDataMap = new HashMap<String, EjbMetaData<?>>();
     
   // TODO Should this be static?
   public <T> T lookup(EjbMetaData<T> ejb)
   {
      if (ejb.getEjbLinkJndiName() != null)
      {
         return JNDI.lookup(ejb.getEjbLinkJndiName(), ejb.getType());
      }
      try
      {
         return JNDI.lookup(ejb.getDefaultJndiName(), ejb.getType());
      }
      catch (Exception e) 
      {
         throw new CreationException("could not find the EJB in JNDI", e);
      }
   }
   
   @SuppressWarnings("unchecked")
   public <T> EjbMetaData<T> registerEjbMetaData(Class<? extends T> clazz)
   {
      // TODO replace with an application lookup
      if (!ejbMetaDataMap.containsKey(clazz))
      {
         EjbMetaData<T> ejbMetaData = new EjbMetaData<T>(clazz); 
         ejbMetaDataMap.put(ejbMetaData.getEjbName(), ejbMetaData);
         return ejbMetaData;
      }
      else
      {
         return (EjbMetaData<T>) ejbMetaDataMap.get(clazz);
      }
      
   }
}
