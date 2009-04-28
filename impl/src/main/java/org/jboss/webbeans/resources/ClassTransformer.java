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
package org.jboss.webbeans.resources;

import java.lang.annotation.Annotation;
import java.util.concurrent.Callable;

import org.jboss.webbeans.bootstrap.api.Service;
import org.jboss.webbeans.introspector.AnnotatedAnnotation;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.jlr.AnnotatedAnnotationImpl;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.util.collections.ConcurrentCache;

public class ClassTransformer implements Service 
{
   
   private final ConcurrentCache<Class<?>, AnnotatedClass<?>> classes;
   private final ConcurrentCache<Class<?>, AnnotatedAnnotation<?>> annotations;
   private final ClassTransformer transformer = this;
   
   /**
    * 
    */
   public ClassTransformer()
   {
      classes = new ConcurrentCache<Class<?>, AnnotatedClass<?>>();
      annotations = new ConcurrentCache<Class<?>, AnnotatedAnnotation<?>>();
   }
   
   public <T> AnnotatedClass<T> classForName(final Class<T> clazz)
   {
      return classes.putIfAbsent(clazz, new Callable<AnnotatedClass<T>>()
      {

         public AnnotatedClass<T> call() throws Exception
         {
            return AnnotatedClassImpl.of(clazz, transformer);
         }
            
      });
   }
   
   public <T extends Annotation> AnnotatedAnnotation<T> classForName(final Class<T> clazz)
   {
      return annotations.putIfAbsent(clazz, new Callable<AnnotatedAnnotation<T>>()
      {

         public AnnotatedAnnotation<T> call() throws Exception
         {
            return AnnotatedAnnotationImpl.of(clazz, transformer);
         }
            
      });
   }

}
