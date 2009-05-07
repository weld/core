/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.webbeans.injection.resolution;

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
   
   public static <T> ResolvableAnnotatedClass<T> of(Type type, Annotation[] annotations)
   {
      if (type instanceof Class)
      {
         return new ResolvableAnnotatedClass<T>((Class<T>) type, type, annotations);
      }
      else if (type instanceof ParameterizedType)
      {
         return new ResolvableAnnotatedClass<T>((Class<T>) ((ParameterizedType) type).getRawType(), type, annotations);
      }
      else 
      {
         throw new UnsupportedOperationException("Cannot create annotated item of " + type);
      }
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

   @Override
   public Type[] getActualTypeArguments()
   {
      return actualTypeArguments;
   }

   public String getName()
   {
      throw new UnsupportedOperationException();
   }

   @Override
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
   
   @Override
   public Set<Type> getFlattenedTypeHierarchy()
   {
      throw new UnsupportedOperationException();
   }
   
   @Override
   public boolean isProxyable()
   {
      throw new UnsupportedOperationException();
   }

}