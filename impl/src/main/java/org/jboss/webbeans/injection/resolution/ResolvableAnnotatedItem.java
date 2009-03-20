package org.jboss.webbeans.injection.resolution;

import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.ForwardingAnnotatedItem;

/**
 * Extension of an element which bases equality not only on type, but also on
 * binding type
 */
abstract class ResolvableAnnotatedItem<T, S> extends ForwardingAnnotatedItem<T, S>
{
   
   private static final long serialVersionUID = 1L;

   @Override
   public boolean equals(Object other)
   {
      if (other instanceof AnnotatedItem)
      {
         AnnotatedItem<?, ?> that = (AnnotatedItem<?, ?>) other;
         return delegate().isAssignableFrom(that) && that.getBindings().equals(this.getBindings());
      }
      else
      {
         return false;
      }
   }

   @Override
   public int hashCode()
   {
      return delegate().hashCode();
   }

   @Override
   public String toString()
   {
      return "Resolvable annotated item for " + delegate();
   }

}