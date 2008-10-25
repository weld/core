package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import javax.webbeans.TypeLiteral;

public class SimpleAnnotatedItem<T, S> extends AbstractAnnotatedItem<T, S>
{

   private Type[] actualTypeArguements = new Type[0];
   private Class<? extends T> type;
   
   public SimpleAnnotatedItem(Map<Class<? extends Annotation>, Annotation> annotationMap)
   {
      super(annotationMap);
   }
   
   public SimpleAnnotatedItem(Map<Class<? extends Annotation>, Annotation> annotationMap, Class<? extends T> type)
   {
      super(annotationMap);
      this.type = type;
   }
   
   public SimpleAnnotatedItem(Map<Class<? extends Annotation>, Annotation> annotationMap, TypeLiteral<? extends T> apiType)
   {
      super(annotationMap);
      this.type = apiType.getRawType();
      if (apiType.getType() instanceof ParameterizedType)
      {
         actualTypeArguements = ((ParameterizedType) apiType.getType()).getActualTypeArguments();
      }
   }
   
   public SimpleAnnotatedItem(Map<Class<? extends Annotation>, Annotation> annotationMap, Class<? extends T> type, Type[] actualTypeArguements)
   {
      this(annotationMap, type);
      this.actualTypeArguements = actualTypeArguements;
   }
   
   public SimpleAnnotatedItem(Annotation[] annotations)
   {
      this(buildAnnotationMap(annotations));
   }
   
   public SimpleAnnotatedItem(Annotation[] annotations, Class<? extends T> type)
   {
      this(buildAnnotationMap(annotations), type);
   }
   
   public SimpleAnnotatedItem(Annotation[] annotations, TypeLiteral<? extends T> apiType)
   {
      this(buildAnnotationMap(annotations), apiType);
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
