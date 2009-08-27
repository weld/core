/**
 * 
 */
package org.jboss.webbeans.mock;

import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.webbeans.ejb.api.SessionObjectReference;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;
import org.jboss.webbeans.ejb.spi.EjbServices;

public class MockEjBServices implements EjbServices
{
   
   

   public SessionObjectReference resolveEjb(EjbDescriptor<?> ejbDescriptor)
   {
      return new SessionObjectReference()
      {

         public <S> S getBusinessObject(Class<S> businessInterfaceType)
         {
            // TODO Auto-generated method stub
            return null;
         }

         public void remove()
         {
            // TODO Auto-generated method stub
            
         }
         
         public Object getFieldValue(Class<?> declaringClass, String fieldName)
         {
            // TODO Auto-generated method stub
            return null;
         }
         
      };
   }
}