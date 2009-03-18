package org.jboss.webbeans.bootstrap.api.test;

import java.lang.annotation.Annotation;

import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.ejb.api.SessionObjectReference;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;
import org.jboss.webbeans.ejb.spi.EjbServices;

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
   
   public SessionObjectReference resolveEjb(EjbDescriptor<?> ejbDescriptor)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public Object resolveEjb(InjectionPoint injectionPoint)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public Object resolvePersistenceContext(InjectionPoint injectionPoint)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public Object resolveResource(InjectionPoint injectionPoint)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
}
