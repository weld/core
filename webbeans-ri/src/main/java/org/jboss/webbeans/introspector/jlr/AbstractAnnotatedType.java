package org.jboss.webbeans.introspector.jlr;

import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.util.Reflections;

public abstract class AbstractAnnotatedType<T> extends AbstractAnnotatedItem<T, Class<T>>
{
   
   private AnnotatedClass<Object> superclass;

   public AbstractAnnotatedType(AnnotationMap annotationMap)
   {
      super(annotationMap);
   }
   
   public boolean isStatic()
   {
      return Reflections.isStatic(getDelegate());
   }
   
   public boolean isFinal()
   {
      return Reflections.isFinal(getDelegate());
   }
   
   public String getName()
   {
      return getDelegate().getName();
   }
   
   @SuppressWarnings("unchecked")
   // TODO Fix this
   public AnnotatedClass<Object> getSuperclass()
   {
      if (superclass == null)
      {
         superclass = new AnnotatedClassImpl(getDelegate().getSuperclass());
      }
      return superclass;
   }
   
}
