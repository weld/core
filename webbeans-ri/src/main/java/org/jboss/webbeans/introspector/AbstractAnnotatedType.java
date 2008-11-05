package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.jboss.webbeans.util.Reflections;

public abstract class AbstractAnnotatedType<T> extends AbstractAnnotatedItem<T, Class<T>>
{

   public AbstractAnnotatedType(Map<Class<? extends Annotation>, Annotation> annotationMap)
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
   
}
