/**
 *
 */
package org.jboss.webbeans.mock;

import org.jboss.webbeans.ejb.api.SessionObjectReference;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.ejb.spi.InterceptorBindings;

public class MockEjBServices implements EjbServices
{



   public SessionObjectReference resolveEjb(EjbDescriptor<?> ejbDescriptor)
   {
      return new SessionObjectReference()
      {

         private static final long serialVersionUID = 1L;

         public <S> S getBusinessObject(Class<S> businessInterfaceType)
         {
            // TODO Auto-generated method stub
            return null;
         }

         public void remove()
         {
            // TODO Auto-generated method stub

         }

         public boolean isRemoved()
         {
            // TODO Auto-generated method stub
            return false;
         }

      };
   }

   public void registerInterceptors(EjbDescriptor<?> ejbDescriptor, InterceptorBindings interceptorBindings)
   {
      // do nothing
   }

   public void cleanup() {}

}
