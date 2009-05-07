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
package org.jboss.webbeans.bean.standard;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.event.Event;

import org.jboss.webbeans.injection.resolution.AnnotatedItemTransformer;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.ForwardingAnnotatedItem;

/**
 * AnnotatedItem transformer which can be used for FacadeBeans
 * 
 * @author Pete Muir
 *
 */
public class FacadeBeanAnnotatedItemTransformer implements AnnotatedItemTransformer
{
   
   private final Class<?> clazz;
   private final Annotation annotation;
   private final Set<Annotation> annotations;
   private final Set<Type> flattenedTypes;
   
   public FacadeBeanAnnotatedItemTransformer(Class<?> clazz, Annotation annotation)
   {
      this.clazz = clazz;
      this.annotation = annotation;
      this.annotations = new HashSet<Annotation>(Arrays.asList(annotation));
      Type[] types = {Object.class, Event.class};
      this.flattenedTypes = new HashSet<Type>(Arrays.asList(types));
   }

   public <T, S> AnnotatedItem<T, S> transform(final AnnotatedItem<T, S> element)
   {
      if (clazz.isAssignableFrom(element.getRawType()))
      {
         if (element.isAnnotationPresent(annotation.annotationType()))
         {
            
            return new ForwardingAnnotatedItem<T, S>()
            {
               
               @Override
               public Type[] getActualTypeArguments()
               {
                  return new Type[0];
               }
               
               @Override
               public Set<Annotation> getBindings()
               {
                  return annotations;
               }
               
               @SuppressWarnings("unchecked")
               @Override
               public Class<T> getRawType()
               {
                  return (Class<T>) clazz;
               }
               
               @Override
               public Type getType()
               {
                  return clazz;
               }
               
               @Override
               public Set<Type> getFlattenedTypeHierarchy()
               {
                  return flattenedTypes;
               }

               @Override
               public AnnotatedItem<T, S> delegate()
               {
                  return element;
               }
               
               @Override
               public boolean isAssignableFrom(Set<? extends Type> types)
               {
                  return types.contains(clazz);
               }

            };
         }
      }
      return element;
   }
   
}
