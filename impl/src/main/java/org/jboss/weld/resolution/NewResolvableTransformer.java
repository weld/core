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
package org.jboss.weld.resolution;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.New;

import org.jboss.weld.literal.NewLiteral;
import org.jboss.weld.util.reflection.HierarchyDiscovery;

/**
 * @author pmuir
 *
 */
public class NewResolvableTransformer implements ResolvableTransformer
{

   public Resolvable transform(final Resolvable element)
   {
      if (element.isAnnotationPresent(New.class) && element.getJavaClass() != null)
      {
         New originalNewAnnotation = element.getAnnotation(New.class);
         if (originalNewAnnotation.value().equals(New.class))
         {
            final Set<Annotation> bindings = new HashSet<Annotation>(element.getQualifiers());
            final New newNewAnnotation = new NewLiteral()
            {
               
               @Override
               public Class<?> value()
               {
                  return element.getJavaClass();
               }
               
            };
            bindings.remove(originalNewAnnotation);
            bindings.add(newNewAnnotation);
            return new ForwardingResolvable()
            {
               
               @Override
               protected Resolvable delegate()
               {
                  return element;
               }
               
               @Override
               public Set<Annotation> getQualifiers()
               {
                  return bindings;
               }
               
               @Override
               public <A extends Annotation> A getAnnotation(Class<A> annotationType)
               {
                  if (annotationType.equals(New.class))
                  {
                     return annotationType.cast(newNewAnnotation);
                  }
                  else
                  {
                     return delegate().getAnnotation(annotationType);
                  }
               }
               
            };
         }
         else
         {
            final Class<?> javaClass = originalNewAnnotation.value();
            final Set<Type> typeClosure = new HierarchyDiscovery(javaClass).getTypeClosure();
            return new ForwardingResolvable()
            {
               
               @Override
               protected Resolvable delegate()
               {
                  return element;
               }
               
               @Override
               public Class<?> getJavaClass()
               {
                  return javaClass;
               }
               
               @Override
               public Set<Type> getTypeClosure()
               {
                  return typeClosure;
               }
               
            };
         }
      }
      return element;
   }

}
