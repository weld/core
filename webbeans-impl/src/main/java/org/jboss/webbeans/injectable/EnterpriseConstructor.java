package org.jboss.webbeans.injectable;

import javax.webbeans.Container;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.ejb.EjbMetaData;


public class EnterpriseConstructor<T> implements ComponentConstructor<T>
{

   private EjbMetaData<T> ejbMetaData;
   
   public EnterpriseConstructor(EjbMetaData<T> ejbMetaData)
   {
      this.ejbMetaData = ejbMetaData;
   }
   
   public T invoke(Container container)
   {
      // TODO Hmm, this isn't right
      if (container instanceof ContainerImpl)
      {
         ContainerImpl containerImpl = (ContainerImpl) container;
         return containerImpl.getEjbManager().lookup(ejbMetaData);
      }
      else
      {
         throw new RuntimeException("Error accessing webbeans container");
      }
   }

}
