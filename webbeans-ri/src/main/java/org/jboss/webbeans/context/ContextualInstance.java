package org.jboss.webbeans.context;

import javax.webbeans.manager.Contextual;

public class ContextualInstance<T>
{
   private Contextual<T> contextual;
   private T instance;

   protected ContextualInstance(Contextual<T> contextual, T instance)
   {
      this.contextual = contextual;
      this.instance = instance;
   }

   public static <T> ContextualInstance<T> of(Contextual<T> contextual, T instance)
   {
      return new ContextualInstance<T>(contextual, instance);
   }

   public void destroy()
   {
      contextual.destroy(instance);
   }

}
