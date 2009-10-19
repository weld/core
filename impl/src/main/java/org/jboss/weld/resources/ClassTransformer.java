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
package org.jboss.weld.resources;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.introspector.WeldAnnotation;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.jlr.WeldAnnotationImpl;
import org.jboss.weld.introspector.jlr.WeldClassImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.util.collections.ConcurrentCache;

public class ClassTransformer implements Service
{

   private final ConcurrentCache<Type, WeldClass<?>> classes;
   private final ConcurrentCache<AnnotatedType<?>, WeldClass<?>> annotatedTypes;
   private final ConcurrentCache<Class<?>, WeldAnnotation<?>> annotations;
   private final TypeStore typeStore;

   /**
    * 
    */
   public ClassTransformer(TypeStore typeStore)
   {
      classes = new ConcurrentCache<Type, WeldClass<?>>();
      this.annotatedTypes = new ConcurrentCache<AnnotatedType<?>, WeldClass<?>>();
      annotations = new ConcurrentCache<Class<?>, WeldAnnotation<?>>();
      this.typeStore = typeStore;
   }

   public <T> WeldClass<T> loadClass(final Class<T> rawType, final Type baseType)
   {
      return classes.putIfAbsent(baseType, new Callable<WeldClass<T>>()
      {

         public WeldClass<T> call() throws Exception
         {
            return WeldClassImpl.of(rawType, baseType, ClassTransformer.this);
         }

      });
   }
   
   public <T> WeldClass<T> loadClass(final Class<T> clazz)
   {
      return classes.putIfAbsent(clazz, new Callable<WeldClass<T>>()
      {

         public WeldClass<T> call() throws Exception
         {
            return WeldClassImpl.of(clazz, ClassTransformer.this);
         }

      });
   }
   
   public <T> WeldClass<T> loadClass(final AnnotatedType<T> clazz)
   {
      return annotatedTypes.putIfAbsent(clazz, new Callable<WeldClass<T>>()
      {

         public WeldClass<T> call() throws Exception
         {
            return WeldClassImpl.of(clazz, ClassTransformer.this);
         }

      });
   }

   public <T extends Annotation> WeldAnnotation<T> loadAnnotation(final Class<T> clazz)
   {
      return annotations.putIfAbsent(clazz, new Callable<WeldAnnotation<T>>()
      {
         
         public WeldAnnotation<T> call() throws Exception
         {
            return WeldAnnotationImpl.of(clazz, ClassTransformer.this);
         }

      });
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
