/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.resources;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.introspector.WeldAnnotation;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.jlr.WeldAnnotationImpl;
import org.jboss.weld.introspector.jlr.WeldClassImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.spi.ResourceLoadingException;

import com.google.common.base.Function;
import com.google.common.collect.ComputationException;
import com.google.common.collect.MapMaker;

public class ClassTransformer implements Service
{
   
   private static class TransformTypeToWeldClass implements Function<TypeHolder<?>, WeldClass<?>>
   {
      
      private final ClassTransformer classTransformer;

      private TransformTypeToWeldClass(ClassTransformer classTransformer)
      {
         this.classTransformer = classTransformer;
      }

      public WeldClass<?> apply(TypeHolder<?> from)
      {
         return WeldClassImpl.of(from.getRawType(), from.getBaseType(), classTransformer);
      }
      
   }
   
   private static class TransformClassToWeldAnnotation implements Function<Class<? extends Annotation>, WeldAnnotation<?>>
   {
      
      private final ClassTransformer classTransformer;

      private TransformClassToWeldAnnotation(ClassTransformer classTransformer)
      {
         this.classTransformer = classTransformer;
      }

      public WeldAnnotation<?> apply(Class<? extends Annotation> from)
      {
         return WeldAnnotationImpl.of(from, classTransformer);
      }
      
   }
   
   private static class TransformAnnotatedTypeToWeldClass implements Function<AnnotatedType<?>, WeldClass<?>>
   {
      
      private final ClassTransformer classTransformer;

      private TransformAnnotatedTypeToWeldClass(ClassTransformer classTransformer)
      {
         super();
         this.classTransformer = classTransformer;
      }

      public WeldClass<?> apply(AnnotatedType<?> from)
      {
         return WeldClassImpl.of(from, classTransformer);
      }
      
   }
   
   private static final class TypeHolder<T>
   {
      private final Class<T> rawType;
      private final Type baseType;
      
      private TypeHolder(Class<T> rawType, Type baseType)
      {
         this.rawType = rawType;
         this.baseType = baseType;
      }
      
      public Type getBaseType()
      {
         return baseType;
      }
      
      public Class<T> getRawType()
      {
         return rawType;
      }
      
      @Override
      public boolean equals(Object obj)
      {
         if (obj instanceof TypeHolder<?>)
         {
            TypeHolder<?> that = (TypeHolder<?>) obj;
            return this.getBaseType().equals(that.getBaseType());
         }
         else
         {
            return false;
         }
      }
      
      @Override
      public int hashCode()
      {
         return getBaseType().hashCode();
      }
      
      @Override
      public String toString()
      {
         return getBaseType().toString();
      }
   }

   private final ConcurrentMap<TypeHolder<?>, WeldClass<?>> classes;
   private final ConcurrentMap<AnnotatedType<?>, WeldClass<?>> annotatedTypes;
   private final ConcurrentMap<Class<? extends Annotation>, WeldAnnotation<?>> annotations;
   private final TypeStore typeStore;

   /**
    * 
    */
   public ClassTransformer(TypeStore typeStore)
   {
      MapMaker maker = new MapMaker();
      this.classes = maker.makeComputingMap(new TransformTypeToWeldClass(this));
      this.annotatedTypes = maker.makeComputingMap(new TransformAnnotatedTypeToWeldClass(this));
      this.annotations = maker.makeComputingMap(new TransformClassToWeldAnnotation(this));
      this.typeStore = typeStore;
   }

   @SuppressWarnings("unchecked")
   public <T> WeldClass<T> loadClass(final Class<T> rawType, final Type baseType)
   {
      try
      {
         return (WeldClass<T>) classes.get(new TypeHolder<T>(rawType, baseType));
      }
      catch (ComputationException e)
      {
         if (e.getCause() instanceof NoClassDefFoundError || e.getCause() instanceof TypeNotPresentException)
         {
            throw new ResourceLoadingException("Error loading class " + rawType.getName(), e.getCause());
         }
         else
         {
            throw e;
         }
      }
   }
   
   public <T> WeldClass<T> loadClass(final Class<T> clazz)
   {
      try
      {
         return cast(classes.get(new TypeHolder<T>(clazz, clazz)));
      }
      catch (ComputationException e)
      {
         if (e.getCause() instanceof NoClassDefFoundError || e.getCause() instanceof TypeNotPresentException)
         {
            throw new ResourceLoadingException("Error loading class " + clazz.getName(), e.getCause());
         }
         else
         {
            throw e;
         }
      }
   }
   
   @SuppressWarnings("unchecked")
   public <T> WeldClass<T> loadClass(final AnnotatedType<T> clazz)
   {
      return (WeldClass<T>) annotatedTypes.get(clazz);
   }

   @SuppressWarnings("unchecked")
   public <T extends Annotation> WeldAnnotation<T> loadAnnotation(final Class<T> clazz)
   {
      return (WeldAnnotation<T>) annotations.get(clazz);
   }
   
   public TypeStore getTypeStore()
   {
      return typeStore;
   }
   
   public void cleanup() 
   {
      this.annotatedTypes.clear();
      this.annotations.clear();
      this.classes.clear();
   }

}
