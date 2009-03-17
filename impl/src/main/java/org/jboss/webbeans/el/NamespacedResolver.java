package org.jboss.webbeans.el;


import javax.el.ELContext;
import javax.inject.ExecutionException;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.context.DependentInstancesStore;
import org.jboss.webbeans.context.DependentStorageRequest;

class NamespacedResolver
{
   
   private final ELContext context;
   private final String property;
   private final Namespace namespace;
   private Object value;
   
   public NamespacedResolver(ELContext context, Namespace namespace, String property)
   {
      this.context = context;
      this.property = property;
      this.namespace = namespace;
   }
   
   public Object getValue()
   {
      return value;
   }
   
   public NamespacedResolver run()
   {
         
      try
      {
         new RunInDependentContext()
         {
            
            @Override
            protected void execute() throws Exception
            {
               value = CurrentManager.rootManager().getInstanceByName(namespace.getQualifiedName(property));
               if (value != null)
               {
                  context.setPropertyResolved(true);
               }
            }
            
         }.run();
      }
      catch (Exception e)
      {
         throw new ExecutionException("Error resolving EL " + property);
      }
      
      if (!context.isPropertyResolved())
      {
         // look for a namespace
         value = namespace.getChild(property);
         if (value != null) 
         {
             context.setPropertyResolved(true);
         }
      }
      return this;
   }
   
   static abstract class RunInDependentContext
   {
      
      private final DependentStorageRequest dependentStorageRequest;
      
      public RunInDependentContext()
      {
         dependentStorageRequest = DependentStorageRequest.of(new DependentInstancesStore(), new Object());
      }
      
      private void setup()
      {
         DependentContext.INSTANCE.setActive(true);
         DependentContext.INSTANCE.startCollectingDependents(dependentStorageRequest);
      }
      
      private void cleanup()
      {
         DependentContext.INSTANCE.stopCollectingDependents(dependentStorageRequest);
         // TODO kinky
         dependentStorageRequest.getDependentInstancesStore().destroyDependentInstances(dependentStorageRequest.getKey());
         DependentContext.INSTANCE.setActive(false);
      }
      
      protected abstract void execute() throws Exception;
      
      public final void run() throws Exception
      {
         try
         {
            setup();
            execute();
         }
         finally
         {
            cleanup();
         }
      }
      
   }
   
}