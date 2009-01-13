package org.jboss.webbeans.test.mock;

import java.lang.annotation.Annotation;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.naming.Context;
import javax.persistence.PersistenceContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.webbeans.InjectionPoint;

import org.jboss.webbeans.bootstrap.WebBeansBootstrap;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.context.ApplicationContext;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.context.RequestContext;
import org.jboss.webbeans.context.SessionContext;
import org.jboss.webbeans.context.beanmap.SimpleBeanMap;
import org.jboss.webbeans.ejb.spi.EjbResolver;
import org.jboss.webbeans.resource.DefaultNaming;
import org.jboss.webbeans.resources.spi.Naming;
import org.jboss.webbeans.resources.spi.ResourceLoader;

public class MockBootstrap extends WebBeansBootstrap
{ 
   
   public static class MockNaming implements Naming
   {
      
      private Context context;
      
      private Naming delegate;
      
      public MockNaming()
      {
         this.delegate = new DefaultNaming();
      }
      
      public void setContext(Context context)
      {
         this.context = context;
      }
      
      public Context getContext()
      {
         return context;
      }

      public void bind(String key, Object value)
      {
         if (context != null)
         {
            delegate.bind(key, value);
         }
      }

      public <T> T lookup(String name, Class<? extends T> expectedType)
      {
         if (context != null)
         {
            T instance = overrideLookup(name, expectedType);
            if (instance == null)
            {
               instance = delegate.lookup(name, expectedType);
            }
            return instance;
         }
         else
         {
            return null;
         }
      }
      
      @SuppressWarnings("unchecked")
      private <T> T overrideLookup(String name, Class<? extends T> expectedType)
      {
         // JBoss Embedded EJB 3.1 doesn't seem to bind this!
         if (name.equals("java:comp/UserTransaction"))
         {
            final TransactionManager tm = delegate.lookup("java:/TransactionManager", TransactionManager.class);
            return (T) new UserTransaction()
            {

               public void begin() throws NotSupportedException, SystemException
               {
                  tm.begin();
               }

               public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException
               {
                  tm.commit();
               }

               public int getStatus() throws SystemException
               {
                  return tm.getStatus();
               }

               public void rollback() throws IllegalStateException, SecurityException, SystemException
               {
                  tm.rollback();
               }

               public void setRollbackOnly() throws IllegalStateException, SystemException
               {
                  tm.setRollbackOnly();
               }

               public void setTransactionTimeout(int seconds) throws SystemException
               {
                  tm.setTransactionTimeout(seconds);
               }
               
            };
         }
         else
         {
            return null;
         }
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
   
   private MockNaming mockNaming;
   
   public MockBootstrap()
   {
      this.resourceLoader = new MockResourceLoader();
      this.mockNaming = new MockNaming();
      initManager(mockNaming, MOCK_EJB_RESOLVER, resourceLoader);
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
   
   public MockNaming getNaming()
   {
      return mockNaming;
   }
   
}
