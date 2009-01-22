package org.jboss.webbeans.introspector;




public abstract class ForwardingAnnotatedType<T> extends ForwardingAnnotatedItem<T, Class<T>> implements AnnotatedType<T>
{

   @Override
   protected abstract AnnotatedType<T> delegate();

   public AnnotatedType<?> getSuperclass()
   {
      return delegate().getSuperclass();
   }

   public boolean isEquivalent(Class<?> clazz)
   {
      return delegate().isEquivalent(clazz);
   }
   
   public String getSimpleName()
   {
      return delegate().getSimpleName();
   }
   
}
