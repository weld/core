package org.jboss.webbeans.introspector;

public interface AnnotatedType<T> extends AnnotatedItem<T, Class<T>>
{
 
   public AnnotatedType<Object> getSuperclass();
   
}
