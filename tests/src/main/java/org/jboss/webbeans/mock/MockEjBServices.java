/**
 * 
 */
package org.jboss.webbeans.mock;

import java.util.Collection;

import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.webbeans.ejb.api.SessionObjectReference;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;
import org.jboss.webbeans.ejb.spi.EjbServices;

public class MockEjBServices implements EjbServices
{
   
   public Object resolveEjb(InjectionPoint injectionPoint)
   {
      return null;
   }
   
   public void removeEjb(Collection<Object> instance)
   {
      // No-op
   }
   
   public Object resolveRemoteEjb(String jndiName, String mappedName, String ejbLink)
   {
      return null;
   }

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