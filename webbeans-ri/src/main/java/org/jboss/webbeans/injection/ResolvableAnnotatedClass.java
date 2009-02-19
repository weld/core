package org.jboss.webbeans.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import javax.inject.TypeLiteral;

import org.jboss.webbeans.introspector.AnnotationStore;
import org.jboss.webbeans.introspector.jlr.AbstractAnnotatedItem;

/**
 * Extension of an element which bases equality not only on type, but also on
 * binding type
 */
public class ResolvableAnnotatedClass<T> extends AbstractAnnotatedItem<T, Class<T>>
{
   
   private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];
   private final Class<T> rawType;
   private final Type[] actualTypeArguments;
   
   private final String _string;
   
   public static <T> ResolvableAnnotatedClass<T> of(TypeLiteral<T> typeLiteral, Annotation[] annotations)
   {
      return new ResolvableAnnotatedClass<T>(typeLiteral.getRawType(), typeLiteral.getType(), annotations);
   }
   
   public static <T> ResolvableAnnotatedClass<T> of(Class<T> clazz, Annotation[] annotations)
   {
      return new ResolvableAnnotatedClass<T>(clazz, clazz, annotations);
   }
   
   private ResolvableAnnotatedClass(Class<T> rawType, Type type, Annotation[] annotations)
   {
      super(AnnotationStore.of(annotations, EMPTY_ANNOTATION_ARRAY));
      this.rawType = rawType;
      if (type instanceof ParameterizedType)
      {
         this.actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
         this._string = rawType.toString() + "<" + Arrays.asList(actualTypeArguments).toString() + ">; binding types  " + annotations;
      }
      else
      {
         this.actualTypeArguments = new Type[0];
         this._string = rawType.toString() +"; binding types  " + annotations;
      }
   }

   @Override
   public String toString()
   {
      return _string;
   }

   @Override
   public Class<T> getDelegate()
   {
      return rawType;
   }

   public Type[] getActualTypeArguments()
   {
      return actualTypeArguments;
   }

   public String getName()
   {
      throw new UnsupportedOperationException();
   }

   public Class<T> getType()
   {
      return rawType;
   }

   public boolean isFinal()
   {
      throw new UnsupportedOperationException();
   }

   public boolean isPublic()
   {
      throw new UnsupportedOperationException();
   }

   public boolean isStatic()
   {
      throw new UnsupportedOperationException();
   }

}