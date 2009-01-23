package org.jboss.webbeans.util;

import java.lang.reflect.Type;
import java.util.Set;

import javax.webbeans.manager.Bean;

import org.jboss.webbeans.MetaDataCache;
import org.jboss.webbeans.bean.AbstractBean;
import org.jboss.webbeans.bean.EnterpriseBean;

public class Beans
{

   public static boolean isPassivatingBean(Bean<?> bean)
   {
      if (bean instanceof EnterpriseBean)
      {
         return ((EnterpriseBean<?>) bean).getEjbDescriptor().isStateful();
      }
      else
      {
         return MetaDataCache.instance().getScopeModel(bean.getScopeType()).isPassivating();
      }
   }

   /**
    * Indicates if a bean is proxyable
    * 
    * @param bean The bean to test
    * @return True if proxyable, false otherwise
    */
   public static boolean isBeanProxyable(Bean<?> bean)
   {
      if (bean instanceof AbstractBean)
      {
         return ((AbstractBean<?, ?>) bean).isProxyable();
      }
      else
      {
         return Beans.apiTypesAreProxyable(bean.getTypes());
      }
   }

   /**
    * Indicates if a set of types are all proxyable
    * 
    * @param types The types to test
    * @return True if proxyable, false otherwise
    */
   public static boolean apiTypesAreProxyable(Set<Type> types)
   {
      for (Type apiType : types)
      {
         if (Object.class.equals(apiType))
         {
            continue;
         }
         boolean isClass = !((Class<?>) apiType).isInterface();
         if (isClass && !Proxies.isClassProxyable((Class<?>) apiType))
         {
            return false;
         }
      }
      return true;
   }
   
}
