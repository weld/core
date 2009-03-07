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

import org.jboss.webbeans.ejb.spi.EjbResolver;
import org.jboss.webbeans.resources.spi.NamingContext;

final class MockEjBResolver implements EjbResolver
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
}