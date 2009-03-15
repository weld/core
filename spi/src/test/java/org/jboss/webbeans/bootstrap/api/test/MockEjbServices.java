package org.jboss.webbeans.bootstrap.api.test;

import java.lang.annotation.Annotation;

import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.ejb.api.EjbReference;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.resources.spi.NamingContext;

public class MockEjbServices implements EjbServices
{
   
   public Iterable<EjbDescriptor<?>> discoverEjbs()
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public Class<? extends Annotation> getEJBAnnotation()
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public Class<? extends Annotation> getPersistenceContextAnnotation()
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public Class<? extends Annotation> getResourceAnnotation()
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public <T> EjbReference<T> resolveEJB(EjbDescriptor<T> ejbDescriptor, NamingContext namingContext)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public Object resolveEjb(InjectionPoint injectionPoint, NamingContext namingContext)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public Object resolvePersistenceContext(InjectionPoint injectionPoint, NamingContext namingContext)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public Object resolveResource(InjectionPoint injectionPoint, NamingContext namingContext)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
}
