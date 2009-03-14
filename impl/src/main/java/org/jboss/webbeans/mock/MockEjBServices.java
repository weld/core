/**
 * 
 */
package org.jboss.webbeans.mock;

import java.lang.annotation.Annotation;
import java.util.Collection;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.manager.InjectionPoint;
import javax.persistence.PersistenceContext;

import org.jboss.webbeans.ejb.api.EjbReference;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.resources.spi.NamingContext;

final class MockEjBServices implements EjbServices
{
   public Class<? extends Annotation> getEJBAnnotation()
   {
      return EJB.class;
   }
   
   public Class<? extends Annotation> getPersistenceContextAnnotation()
   {
      return PersistenceContext.class;
   }
   
   public Object resolveEjb(InjectionPoint injectionPoint, NamingContext namingContext)
   {
      return null;
   }
   
   public Object resolvePersistenceContext(InjectionPoint injectionPoint, NamingContext namingContext)
   {
      return null;
   }
   
   public Class<? extends Annotation> getResourceAnnotation()
   {
      return Resource.class;
   }
   
   public Object resolveResource(InjectionPoint injectionPoint, NamingContext namingContext)
   {
      return null;
   }
   
   public void removeEjb(Collection<Object> instance)
   {
      // No-op
   }

   public <T> EjbReference<T> resolveEJB(EjbDescriptor<T> ejbDescriptor, NamingContext naming)
   {
      return new EjbReference<T>()
      {

         public <S> S get(Class<S> businessInterfaceType)
         {
            return null;
         }

         public void remove()
         {
            // No-op
         }
         
         public void create()
         {
            // No-op
         }
         
      };
   }
}