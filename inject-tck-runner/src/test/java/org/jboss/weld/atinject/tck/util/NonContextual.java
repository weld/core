package org.jboss.weld.atinject.tck.util;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;

/**
 * Perform production, injection, lifecycle callbacks and cleanup on a non-contextual object
 * 
 * @author pmuir
 *
 * @param <T>
 */
public class NonContextual<T>
{

   // Store the injection target. The CDI spec doesn't require an implementation
   // to cache it, so we do
   private final InjectionTarget<T> injectionTarget;

   // Store a reference to the CDI BeanManager
   private final BeanManager beanManager;

   /**
    * Create an injector for the given class
    */
   public NonContextual(BeanManager manager, Class<T> clazz)
   {
      this.beanManager = manager;

      // Generate an "Annotated Type"
      AnnotatedType<T> type = manager.createAnnotatedType(clazz);

      // Generate the InjectionTarget
      this.injectionTarget = manager.createInjectionTarget(type);
   }

   public Instance<T> newInstance()
   {
      return new Instance<T>(beanManager, injectionTarget);
   }

   /**
    * Represents a non-contextual instance
    * 
    * @author pmuir
    * 
    * @param <T>
    */
   public static class Instance<T>
   {

      private final CreationalContext<T> ctx;
      private final InjectionTarget<T> injectionTarget;
      private T instance;
      private boolean disposed = false;

      private Instance(BeanManager beanManager, InjectionTarget<T> injectionTarget)
      {
         this.injectionTarget = injectionTarget;
         this.ctx = beanManager.createCreationalContext(null);
      }

      /**
       * Get the instance
       * 
       * @return
       */
      public T get()
      {
         return instance;
      }

      /**
       * Create the instance
       * 
       * @return
       */
      public Instance<T> produce()
      {
         if (this.instance != null)
         {
            throw new IllegalStateException("Trying to call produce() on already constructed instance");
         }
         if (disposed)
         {
            throw new IllegalStateException("Trying to call produce() on an already disposed instance");
         }
         this.instance = injectionTarget.produce(ctx);
         return this;
      }

      /**
       * Inject the instance
       * 
       * @return
       */
      public Instance<T> inject()
      {
         if (this.instance == null)
         {
            throw new IllegalStateException("Trying to call inject() before produce() was called");
         }
         if (disposed)
         {
            throw new IllegalStateException("Trying to call inject() on already disposed instance");
         }
         injectionTarget.inject(instance, ctx);
         return this;
      }

      /**
       * Call the @PostConstruct callback
       * 
       * @return
       */
      public Instance<T> postConstruct()
      {
         if (this.instance == null)
         {
            throw new IllegalStateException("Trying to call postConstruct() before produce() was called");
         }
         if (disposed)
         {
            throw new IllegalStateException("Trying to call preDestroy() on already disposed instance");
         }
         injectionTarget.postConstruct(instance);
         return this;
      }

      /**
       * Call the @PreDestroy callback
       * 
       * @return
       */
      public Instance<T> preDestroy()
      {
         if (this.instance == null)
         {
            throw new IllegalStateException("Trying to call preDestroy() before produce() was called");
         }
         if (disposed)
         {
            throw new IllegalStateException("Trying to call preDestroy() on already disposed instance");
         }
         injectionTarget.preDestroy(instance);
         return this;
      }

      /**
       * Dispose of the instance, doing any necessary cleanup
       * 
       */
      public Instance<T> dispose()
      {
         if (this.instance == null)
         {
            throw new IllegalStateException("Trying to call dispose() before produce() was called");
         }
         if (disposed)
         {
            throw new IllegalStateException("Trying to call dispose() on already disposed instance");
         }
         injectionTarget.dispose(instance);
         ctx.release();
         return this;
      }

   }

}