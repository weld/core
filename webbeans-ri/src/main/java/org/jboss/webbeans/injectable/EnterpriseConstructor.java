package org.jboss.webbeans.injectable;

import javax.webbeans.manager.EnterpriseBeanLookup;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.ejb.EjbMetaData;


public class EnterpriseConstructor<T> implements BeanConstructor<T, Object>
{

   private EjbMetaData<T> ejbMetaData;
   
   public EnterpriseConstructor(EjbMetaData<T> ejbMetaData)
   {
      this.ejbMetaData = ejbMetaData;
   }
   
   public T invoke(ManagerImpl manager)
   {
      // TODO Hmm, this isn't right
      if (manager instanceof ManagerImpl)
      {
         ManagerImpl containerImpl = (ManagerImpl) manager;
         return (T) containerImpl.getInstanceByType(EnterpriseBeanLookup.class).lookup(ejbMetaData.getEjbName());
      }
      else
      {
         throw new RuntimeException("Error accessing webbeans container");
      }
   }

   public T invoke(ManagerImpl manager, Object instance)
   {
      return invoke(manager);
   }

}
