package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import javax.webbeans.TypeLiteral;

public class SimpleAnnotatedItem<T, S> extends AbstractAnnotatedItem<T, S>
{

   private Type[] actualTypeArguments = new Type[0];
   private Class<? extends T> type;
   private Annotation[] actualAnnotations;
   
   private SimpleAnnotatedItem(Map<Class<? extends Annotation>, Annotation> annotationMap)
   {
      super(annotationMap);
   }
   
   private SimpleAnnotatedItem(Map<Class<? extends Annotation>, Annotation> annotationMap, Class<? extends T> type)
   {
      super(annotationMap);
      this.type = type;
   }
   
   private SimpleAnnotatedItem(Map<Class<? extends Annotation>, Annotation> annotationMap, TypeLiteral<? extends T> apiType)
   {
      super(annotationMap);
      this.type = apiType.getRawType();
      if (apiType.getType() instanceof ParameterizedType)
      {
         actualTypeArguments = ((ParameterizedType) apiType.getType()).getActualTypeArguments();
      }
   }
   
   private SimpleAnnotatedItem(Map<Class<? extends Annotation>, Annotation> annotationMap, Class<? extends T> type, Type[] actualTypeArguments)
   {
      this(annotationMap, type);
      this.actualTypeArguments = actualTypeArguments;
   }
   
   public SimpleAnnotatedItem(Annotation[] annotations)
   {
      this(buildAnnotationMap(annotations));
      this.actualAnnotations = annotations;
   }
   
   public SimpleAnnotatedItem(Annotation[] annotations, Class<? extends T> type)
   {
      this(buildAnnotationMap(annotations), type);
      this.actualAnnotations = annotations;
   }
   
   public SimpleAnnotatedItem(Annotation[] annotations, TypeLiteral<? extends T> apiType)
   {
      this(buildAnnotationMap(annotations), apiType);
      this.actualAnnotations = annotations;
   }
   
   public SimpleAnnotatedItem(Annotation[] annotations, Class<? extends T> type, Type[] actualTypeArguments)
   {
      this(buildAnnotationMap(annotations), type, actualTypeArguments);
      this.actualAnnotations = annotations;
   }

   public S getDelegate()
   {
      return null;
   }
   
   public Class<? extends T> getType()
   {
      return type;
   }

   public Type[] getActualTypeArguments()
   {
      return actualTypeArguments;
   }
   
   public Annotation[] getActualAnnotations()
   {
      return actualAnnotations;
   }
   
}
