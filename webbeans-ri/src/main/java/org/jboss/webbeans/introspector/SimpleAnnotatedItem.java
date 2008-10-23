package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.Map;

public class SimpleAnnotatedItem<T, S> extends AbstractAnnotatedItem<T, S>
{

   private Class<? extends T> type;
   
   public SimpleAnnotatedItem(Map<Class<? extends Annotation>, Annotation> annotationMap)
   {
      this(annotationMap, null);
   }
   
   public SimpleAnnotatedItem(Map<Class<? extends Annotation>, Annotation> annotationMap, Class<? extends T> type)
   {
      super(annotationMap);
      this.type = type;
   }
   
   public SimpleAnnotatedItem(Annotation[] annotations)
   {
      this(annotations, null);
   }
   
   public SimpleAnnotatedItem(Annotation[] annotations, Class<? extends T> type)
   {
      this(buildAnnotationMap(annotations), type);
   }

   public S getDelegate()
   {
      return null;
   }
   
   public Class<? extends T> getType()
   {
      return type;
   }

}
