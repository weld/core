package org.jboss.webbeans.injectable;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.ejb.EjbMetaData;


public class EnterpriseConstructor<T> implements BeanConstructor<T>
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
         return containerImpl.getEjbManager().lookup(ejbMetaData);
      }
      else
      {
         throw new RuntimeException("Error accessing webbeans container");
      }
   }

}
