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
import org.jboss.webbeans.bootstrap.spi.EjbDiscovery;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.ejb.spi.EjbResolver;
import org.jboss.webbeans.resource.AbstractNaming;
import org.jboss.webbeans.resources.spi.Naming;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.test.mock.context.MockApplicationContext;
import org.jboss.webbeans.test.mock.context.MockDependentContext;
import org.jboss.webbeans.test.mock.context.MockRequestContext;
import org.jboss.webbeans.test.mock.context.MockSessionContext;

public class MockBootstrap extends WebBeansBootstrap
{ 
   
   public static class MockNaming extends AbstractNaming
   {
      
      private Context context;
      
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
            super.bind(key, value);
         }
      }

      public <T> T lookup(String name, Class<? extends T> expectedType)
      {
         if (context != null)
         {
            T instance = overrideLookup(name, expectedType);
            if (instance == null)
            {
               instance = super.lookup(name, expectedType);
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
            final TransactionManager tm = super.lookup("java:/TransactionManager", TransactionManager.class);
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
   private EjbDiscovery ejbDiscovery;
   private ResourceLoader resourceLoader;
   
   private MockNaming mockNaming;
   
   public MockBootstrap()
   {
      this.resourceLoader = new MockResourceLoader();
      this.mockNaming = new MockNaming();
      initManager(mockNaming, MOCK_EJB_RESOLVER, resourceLoader);
      registerStandardBeans();
      setupContexts();
   }
   
   protected void setupContexts()
   {
      getManager().addContext(new MockRequestContext());
      getManager().addContext(new MockSessionContext());
      getManager().addContext(new MockApplicationContext());
      getManager().addContext(new MockDependentContext());
   }
   
   protected void registerStandardBeans()
   {
      getManager().setBeans(createStandardBeans());
   }
   
   public void setWebBeanDiscovery(WebBeanDiscovery webBeanDiscovery)
   {
      this.webBeanDiscovery = webBeanDiscovery;
      if (webBeanDiscovery != null)
      {
         this.ejbDiscovery = new MockEjbDiscovery(webBeanDiscovery.discoverWebBeanClasses());
      }
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

   @Override
   protected EjbDiscovery getEjbDiscovery()
   {
      return ejbDiscovery;
   }
   
}
