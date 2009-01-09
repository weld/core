package org.jboss.webbeans.test.mock;

import java.lang.annotation.Annotation;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.persistence.PersistenceContext;
import javax.webbeans.InjectionPoint;

import org.jboss.webbeans.bootstrap.WebBeansBootstrap;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.context.ApplicationContext;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.context.RequestContext;
import org.jboss.webbeans.context.SessionContext;
import org.jboss.webbeans.context.beanmap.SimpleBeanMap;
import org.jboss.webbeans.ejb.spi.EjbResolver;
import org.jboss.webbeans.resources.spi.Naming;
import org.jboss.webbeans.resources.spi.ResourceLoader;

public class MockBootstrap extends WebBeansBootstrap
{ 
   
   private static final Naming MOCK_NAMING = new Naming()
   {

      public void bind(String key, Object value)
      {
         // no-op
      }

      public <T> T lookup(String name, Class<? extends T> expectedType)
      {
         // No-op
         return null;
      }
      
   };
   
   private static final EjbResolver MOCK_EJB_RESOLVER = new EjbResolver()
   {

      public Class<? extends Annotation> getEJBAnnotation()
      {
         return EJB.class;
      }

      public Class<? extends Annotation> getPersistenceContextAnnotation()
      {
         return PersistenceContext.class;
      }

      public Object resolveEjb(InjectionPoint injectionPoint, Naming naming)
      {
         // TODO Implement EJB resolution for Unit tests
         return null;
      }

      public Object resolvePersistenceContext(InjectionPoint injectionPoint, Naming naming)
      {
         // TODO Implement PU resolution for Unit tests
         return null;
      }

      public Class<? extends Annotation> getResourceAnnotation()
      {
         return Resource.class;
      }

      public Object resolveResource(InjectionPoint injectionPoint, Naming naming)
      {
         // TODO Auto-generated method stub
         return null;
      }


      
   };
   
   private WebBeanDiscovery webBeanDiscovery;
   private ResourceLoader resourceLoader;
   
   public MockBootstrap()
   {
      this.resourceLoader = new MockResourceLoader();
      initManager(MOCK_NAMING, MOCK_EJB_RESOLVER, resourceLoader);
      registerStandardBeans();
      
      // Set up the mock contexts
      getManager().addContext(RequestContext.INSTANCE);
      SessionContext.INSTANCE.setBeanMap(new SimpleBeanMap());
      getManager().addContext(SessionContext.INSTANCE);
      ApplicationContext.INSTANCE.setBeanMap(new SimpleBeanMap());
      getManager().addContext(ApplicationContext.INSTANCE);
      getManager().addContext(DependentContext.INSTANCE);
   }
   
   protected void registerStandardBeans()
   {
      getManager().setBeans(createStandardBeans());
   }
   
   public void setWebBeanDiscovery(WebBeanDiscovery webBeanDiscovery)
   {
      this.webBeanDiscovery = webBeanDiscovery;
   }
   
   @Override
   protected WebBeanDiscovery getWebBeanDiscovery()
   {
      return this.webBeanDiscovery;
   }

   @Override
   public ResourceLoader getResourceLoader()
   {
      return resourceLoader;
   }
   
}
