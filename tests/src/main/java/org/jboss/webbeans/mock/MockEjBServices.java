/**
 * 
 */
package org.jboss.webbeans.mock;

import java.lang.reflect.Field;
import java.util.Collection;

import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.ejb.api.SessionObjectReference;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;
import org.jboss.webbeans.ejb.spi.EjbServices;

public class MockEjBServices implements EjbServices
{
   
   private final MockEjbDiscovery ejbDiscovery;
   
   public MockEjBServices(WebBeanDiscovery webBeanDiscovery)
   {
      this.ejbDiscovery = new MockEjbDiscovery(webBeanDiscovery);
   }
   
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
   
   public Iterable<EjbDescriptor<?>> discoverEjbs()
   {
      return ejbDiscovery.discoverEjbs();
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
         
         public Object getFieldValue(Field field)
         {
            // TODO Auto-generated method stub
            return null;
         }
         
      };
   }
}