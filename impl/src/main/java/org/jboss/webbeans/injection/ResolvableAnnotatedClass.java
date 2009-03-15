package org.jboss.webbeans.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.TypeLiteral;

import org.jboss.webbeans.introspector.AnnotationStore;
import org.jboss.webbeans.introspector.jlr.AbstractAnnotatedItem;
import org.jboss.webbeans.util.Names;

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
   
   public static <T> ResolvableAnnotatedClass<T> of(Member member, Annotation[] annotations)
   {
      if (member instanceof Field)
      {
         return new ResolvableAnnotatedClass<T>((Class<T>) ((Field) member).getType(), ((Field) member).getGenericType(), annotations);
      }
      else if (member instanceof Method)
      {
         return new ResolvableAnnotatedClass<T>((Class<T>) ((Method) member).getReturnType(), ((Method) member).getGenericReturnType(), annotations);
      }
      else
      {
         throw new IllegalStateException();
      }
   }
   
   private ResolvableAnnotatedClass(Class<T> rawType, Type type, Annotation[] annotations)
   {
      super(AnnotationStore.of(annotations, EMPTY_ANNOTATION_ARRAY));
      this.rawType = rawType;
      if (type instanceof ParameterizedType)
      {
         this.actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
         this._string = rawType.toString() + "<" + Arrays.asList(actualTypeArguments).toString() + ">; binding types = " + Names.annotationsToString(new HashSet<Annotation>(Arrays.asList(annotations)));
      }
      else
      {
         this.actualTypeArguments = new Type[0];
         this._string = rawType.toString() +"; binding types = " + Names.annotationsToString(new HashSet<Annotation>(Arrays.asList(annotations)));
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

   public Class<T> getRawType()
   {
      return rawType;
   }
   
   @Override
   public Type getType()
   {
      return getRawType();
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
   
   public Set<? extends Type> getFlattenedTypeHierarchy()
   {
      throw new UnsupportedOperationException();
   }
   
   @Override
   public boolean isProxyable()
   {
      throw new UnsupportedOperationException();
   }

}