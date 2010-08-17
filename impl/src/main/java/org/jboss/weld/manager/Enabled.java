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
package org.jboss.weld.manager;

import static com.google.common.collect.Lists.transform;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.resources.spi.ResourceLoader;

import com.google.common.base.Function;

/**
 * 
 * @author Nicklas Karlsson
 * 
 */
public class Enabled
{
   
   private static class ClassLoader<T> implements Function<String, Class<? extends T>>
   {
      
      private final ResourceLoader resourceLoader;
      
      public ClassLoader(ResourceLoader resourceLoader)
      {
         this.resourceLoader = resourceLoader;
      }

      public Class<? extends T> apply(String from)
      {
         return (Class<? extends T>) resourceLoader.classForName(from);
      }
      
   }
   
   public static Enabled of(BeansXml beansXml, ResourceLoader resourceLoader)
   {
      if (beansXml == null)
      {
         return EMPTY_ENABLED;
      }
      else
      {
         ClassLoader<Object> classLoader = new ClassLoader<Object>(resourceLoader);
         ClassLoader<Annotation> annotationLoader = new ClassLoader<Annotation>(resourceLoader);
         return new Enabled(
               transform(beansXml.getEnabledAlternativeStereotypes(), annotationLoader),
               transform(beansXml.getEnabledAlternativeClasses(), classLoader),
               transform(beansXml.getEnabledDecorators(), classLoader),
               transform(beansXml.getEnabledInterceptors(), classLoader)
            );
      }
   }
   
   public static final Enabled EMPTY_ENABLED = new Enabled(Collections.<Class<? extends Annotation>>emptyList(), Collections.<Class<?>>emptyList(), Collections.<Class<?>>emptyList(), Collections.<Class<?>>emptyList());

   private final List<Class<? extends Annotation>> alternativeStereotypes;
   private final List<Class<?>> alternativeClasses;
   private final List<Class<?>> decorators;
   private final List<Class<?>> interceptors;

   private Enabled(List<Class<? extends Annotation>> alternativeStereotypes, List<Class<?>> alternativeClasses, List<Class<?>> decorators, List<Class<?>> interceptors)
   {
      this.alternativeStereotypes = alternativeStereotypes;
      this.alternativeClasses = alternativeClasses;
      this.decorators = decorators;
      this.interceptors = interceptors;
   }

   public List<Class<? extends Annotation>> getAlternativeStereotypes()
   {
      return Collections.unmodifiableList(alternativeStereotypes);
   }

   public List<Class<?>> getAlternativeClasses()
   {
      return Collections.unmodifiableList(alternativeClasses);
   }

   public List<Class<?>> getDecorators()
   {
      return Collections.unmodifiableList(decorators);
   }

   public List<Class<?>> getInterceptors()
   {
      return Collections.unmodifiableList(interceptors);
   }
}
