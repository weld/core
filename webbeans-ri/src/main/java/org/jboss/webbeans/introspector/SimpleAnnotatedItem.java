package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.Map;

public class SimpleAnnotatedItem<E> extends AbstractAnnotatedItem<E>
{

   public SimpleAnnotatedItem(Map<Class<? extends Annotation>, Annotation> annotationMap)
   {
      super(annotationMap);
   }

   public E getDelegate()
   {
      return null;
   }

}
