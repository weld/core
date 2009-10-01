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
package org.jboss.webbeans.resolution;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.TypeLiteral;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.injection.WBInjectionPoint;
import org.jboss.webbeans.introspector.AnnotationStore;
import org.jboss.webbeans.introspector.WBAnnotated;
import org.jboss.webbeans.introspector.jlr.AbstractWBAnnotated;
import org.jboss.webbeans.metadata.TypeStore;
import org.jboss.webbeans.util.Names;
import org.jboss.webbeans.util.Reflections;

public class ResolvableWBClass<T> extends AbstractWBAnnotated<T, Class<T>> implements Resolvable
{

   private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];
   private static final Set<Annotation> EMPTY_ANNOTATION_SET = Collections.emptySet();

   private final Class<T> rawType;
   private final Set<Type> typeClosure;
   private final Type[] actualTypeArguments;

   private final String _string;

   private final BeanManagerImpl manager;

   public static <T> WBAnnotated<T, Class<T>> of(TypeLiteral<T> typeLiteral, Annotation[] annotations, BeanManagerImpl manager)
   {
      return new ResolvableWBClass<T>(typeLiteral.getType(), annotations, manager);
   }

   public static <T> WBAnnotated<T, Class<T>> of(Type type, Annotation[] annotations, BeanManagerImpl manager)
   {
      return new ResolvableWBClass<T>(type, annotations, manager);
   }

   public static <T> WBAnnotated<T, Class<T>> of(InjectionPoint injectionPoint, BeanManagerImpl manager)
   {
      if (injectionPoint instanceof WBInjectionPoint)
      {
         @SuppressWarnings("unchecked")
         WBAnnotated<T, Class<T>> ip = (WBAnnotated<T, Class<T>>) injectionPoint;
         return ip;
      }
      else
      {
         return new ResolvableWBClass<T>(injectionPoint.getType(), injectionPoint.getAnnotated().getAnnotations(), manager);
      }
   }

   public static <T> WBAnnotated<T, Class<T>> of(Member member, Annotation[] annotations, BeanManagerImpl manager)
   {
      if (member instanceof Field)
      {
         return new ResolvableWBClass<T>(((Field) member).getGenericType(), annotations, manager);
      }
      else if (member instanceof Method)
      {
         return new ResolvableWBClass<T>(((Method) member).getGenericReturnType(), annotations, manager);
      }
      else
      {
         throw new IllegalStateException();
      }
   }

   private ResolvableWBClass(Type type, AnnotationStore annotationStore, BeanManagerImpl manager)
   {
      super(annotationStore);

      this.manager = manager;

      if (type instanceof ParameterizedType)
      {
         ParameterizedType parameterizedType = (ParameterizedType) type;
         if (parameterizedType.getRawType() instanceof Class)
         {
            this.rawType = (Class<T>) parameterizedType.getRawType();
         }
         else
         {
            throw new IllegalArgumentException("Cannot extract rawType from " + type);
         }
         this.actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
         this._string = rawType.toString() + "<" + Arrays.asList(actualTypeArguments).toString() + ">; binding types = " + Names.annotationsToString(annotationStore.getBindings());
      }
      else if (type instanceof Class)
      {
         this.rawType = (Class<T>) type;
         this.actualTypeArguments = new Type[0];
         this._string = rawType.toString() +"; binding types = " + Names.annotationsToString(annotationStore.getBindings());
      }
      else
      {
         throw new IllegalArgumentException("Unable to extract type information from " + type);
      }
      this.typeClosure = new HashSet<Type>();
      typeClosure.add(type);
   }

   private ResolvableWBClass(Type type, Annotation[] annotations, BeanManagerImpl manager)
   {
      this(type, AnnotationStore.of(annotations, EMPTY_ANNOTATION_ARRAY, manager.getServices().get(TypeStore.class)), manager);
   }

   private ResolvableWBClass(Type type, Set<Annotation>annotations, BeanManagerImpl manager)
   {
      this(type, AnnotationStore.of(annotations, EMPTY_ANNOTATION_SET, manager.getServices().get(TypeStore.class)), manager);
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
   public Class<T> getJavaClass()
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
   
   public boolean isPrivate()
   {
      throw new UnsupportedOperationException();
   }
   
   public boolean isPackagePrivate()
   {
      throw new UnsupportedOperationException();
   }
   
   public Package getPackage()
   {
      throw new UnsupportedOperationException();
   }

   public boolean isStatic()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean isProxyable()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public Set<Type> getTypeClosure()
   {
      return typeClosure;
   }
   
   public AbstractClassBean<?> getDeclaringBean()
   {
      return null;
   }

   @Override
   public Type getBaseType()
   {
      return getJavaClass();
   }

   public boolean isAssignableTo(Class<?> clazz)
   {
      return Reflections.isAssignableFrom(clazz, getBaseType());
   }

}