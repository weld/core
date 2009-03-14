package org.jboss.webbeans.ejb.spi.helpers;

import java.lang.annotation.Annotation;

import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.ejb.api.EjbReference;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.resources.spi.NamingContext;

/**
 * An implementation of {@link EjbServices} which forwards all its method calls
 * to another {@link EjbServices}}. Subclasses should override one or more 
 * methods to modify the behavior of the backing {@link EjbServices} as desired
 * per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 * 
 * @author Pete Muir
 *
 */
public abstract class ForwardingEjbServices implements EjbServices
{
   
   public abstract EjbServices delegate();
   
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
   
   public Object resolveEjb(InjectionPoint injectionPoint, NamingContext namingContext)
   {
      return delegate().resolveEjb(injectionPoint, namingContext);
   }
   
   public <T> EjbReference<T> resolveEJB(EjbDescriptor<T> ejbDescriptor, NamingContext namingContext)
   {
      return delegate().resolveEJB(ejbDescriptor, namingContext);
   }
   
   public Object resolvePersistenceContext(InjectionPoint injectionPoint, NamingContext namingContext)
   {
      return delegate().resolvePersistenceContext(injectionPoint, namingContext);
   }
   
   public Object resolveResource(InjectionPoint injectionPoint, NamingContext namingContext)
   {
      return delegate().resolveResource(injectionPoint, namingContext);
   }
//   
//   public void removeEjb(Collection<Object> instance)
//   {
//      delegate().removeEjb(instance);
//   }
   
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
