package org.jboss.webbeans.injectable;

import javax.webbeans.manager.Manager;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.ejb.EjbMetaData;


public class EnterpriseConstructor<T> implements ComponentConstructor<T>
{

   private EjbMetaData<T> ejbMetaData;
   
   public EnterpriseConstructor(EjbMetaData<T> ejbMetaData)
   {
      this.ejbMetaData = ejbMetaData;
   }
   
   public T invoke(Manager container)
   {
      // TODO Hmm, this isn't right
      if (container instanceof ManagerImpl)
      {
         ManagerImpl containerImpl = (ManagerImpl) container;
         return containerImpl.getEjbManager().lookup(ejbMetaData);
      }
      else
      {
         throw new RuntimeException("Error accessing webbeans container");
      }
   }

}
