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
   private Map<String, EjbMetaData<?>> ejbMetaDataMap = new HashMap<String, EjbMetaData<?>>();
   
   public Object lookup(String ejbName)
   {
      return lookup( ejbMetaDataMap.get(ejbName) );
   }
   
   public static <T> T lookup(EjbMetaData<T> ejb)
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
   
   //TODO: this method needs to get called at startup
   public <T> EjbMetaData<T> registerEjbMetaData(Class<T> clazz)
   {
      EjbMetaData<T> ejbMetaData = new EjbMetaData<T>(clazz); 
      ejbMetaDataMap.put(ejbMetaData.getEjbName(), ejbMetaData);
      return ejbMetaData;
   }
}
