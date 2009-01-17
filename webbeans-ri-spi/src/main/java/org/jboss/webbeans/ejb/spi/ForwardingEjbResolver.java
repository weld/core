package org.jboss.webbeans.ejb.spi;

import java.lang.annotation.Annotation;

import javax.webbeans.InjectionPoint;

import org.jboss.webbeans.resources.spi.Naming;

/**
 * An implementation of {@link EjbResolver} which forwards all its method calls
 * to another {@link EjbResolver}}. Subclasses should override one or more 
 * methods to modify the behavior of the backing {@link EjbResolver} as desired
 * per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 * 
 * @author Pete Muir
 *
 */
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
   
   @Override
   public boolean equals(Object obj)
   {
      return delegate().equals(obj);
   }
   
   @Override
   public String toString()
   {
      return delegate().toString();
   }
   
   @Override
   public int hashCode()
   {
      return delegate().hashCode();
   }
   
}
