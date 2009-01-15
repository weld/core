package org.jboss.webbeans.ejb.spi;

import java.lang.annotation.Annotation;

import javax.webbeans.InjectionPoint;

import org.jboss.webbeans.resources.spi.Naming;

public abstract class ForwardingEjbResolver implements EjbResolver
{
   
   public abstract EjbResolver delegate();
   
   public Class<? extends Annotation> getEJBAnnotation()
   {
      return delegate().getEJBAnnotation();
   }
   
   public Class<? extends Annotation> getPersistenceContextAnnotation()
   {
      return delegate().getPersistenceContextAnnotation();
   }
   
   public Class<? extends Annotation> getResourceAnnotation()
   {
      return delegate().getResourceAnnotation();
   }
   
   public Object resolveEjb(InjectionPoint injectionPoint, Naming naming)
   {
      return delegate().resolveEjb(injectionPoint, naming);
   }
   
   public Object resolvePersistenceContext(InjectionPoint injectionPoint, Naming naming)
   {
      return delegate().resolvePersistenceContext(injectionPoint, naming);
   }
   
   public Object resolveResource(InjectionPoint injectionPoint, Naming naming)
   {
      return delegate().resolveResource(injectionPoint, naming);
   }
   
}
