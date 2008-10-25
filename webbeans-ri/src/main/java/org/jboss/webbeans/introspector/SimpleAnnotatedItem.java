package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

public class SimpleAnnotatedItem<T, S> extends AbstractAnnotatedItem<T, S>
{

   private Type[] actualTypeArguements = new Type[0];
   
   
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
   
   public SimpleAnnotatedItem(Map<Class<? extends Annotation>, Annotation> annotationMap, Class<? extends T> type, Type[] actualTypeArguements)
   {
      this(annotationMap, type);
      this.actualTypeArguements = actualTypeArguements;
   }
   
   public SimpleAnnotatedItem(Annotation[] annotations)
   {
      this(annotations, null);
   }
   
   public SimpleAnnotatedItem(Annotation[] annotations, Class<? extends T> type)
   {
      this(buildAnnotationMap(annotations), type);
   }
   
   public SimpleAnnotatedItem(Annotation[] annotations, Class<? extends T> type, Type[] actualTypeArguements)
   {
      this(buildAnnotationMap(annotations), type, actualTypeArguements);
   }

   public S getDelegate()
   {
      return null;
   }
   
   public Class<? extends T> getType()
   {
      return type;
   }

   public Type[] getActualTypeArguements()
   {
      return actualTypeArguements;
   }
   
}
