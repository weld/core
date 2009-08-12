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
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.webbeans.introspector.WBAnnotated;
import org.jboss.webbeans.literal.CurrentLiteral;
import org.jboss.webbeans.util.Reflections;

public class ResolvableFactory
{

   public static Resolvable of(WBAnnotated<?, ?> element)
   {
      if (element instanceof Resolvable)
      {
         return (Resolvable) element;
      }
      else
      {
         Set<Type> types = new HashSet<Type>();
         types.add(element.getBaseType());
         return new ResolvableImpl(element.getBindings(), types);
      }
   }

   public static Resolvable of(Set<Type> typeClosure, Set<Annotation> bindings)
   {
      return new ResolvableImpl(bindings, typeClosure);
   }

   public static Resolvable of(Set<Type> typeClosure, Annotation... bindings)
   {
      return new ResolvableImpl(new HashSet<Annotation>(Arrays.asList(bindings)), typeClosure);
   }

   private ResolvableFactory() {}

   private static class ResolvableImpl implements Resolvable
   {

      private final Set<Annotation> bindings;
      private final Map<Class<? extends Annotation>, Annotation> annotations;
      private final Set<Type> typeClosure;

      public ResolvableImpl(Set<Annotation> bindings, Set<Type> typeClosure)
      {
         this.bindings = bindings;
         if (bindings.size() == 0)
         {
            this.bindings.add(new CurrentLiteral());
         }
         this.annotations = new HashMap<Class<? extends Annotation>, Annotation>();
         this.typeClosure = typeClosure;
         for (Annotation annotation : bindings)
         {
            annotations.put(annotation.annotationType(), annotation);
         }
      }

      public Set<Annotation> getBindings()
      {
         return bindings;
      }

      public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
      {
         return annotations.containsKey(annotationType);
      }

      public Set<Type> getTypeClosure()
      {
         return typeClosure;
      }

      public boolean isAssignableTo(Class<?> clazz)
      {
         return Reflections.isAssignableFrom(clazz, typeClosure);
      }
      
      public <A extends Annotation> A getAnnotation(Class<A> annotationType)
      {
         return (A) annotations.get(annotationType);
      }
      
      public Class<?> getJavaClass()
      {
         // No underlying java class
         return null;
      }

      @Override
      public String toString()
      {
         return "Types: " + getTypeClosure() + "; Bindings: " + getBindings();
      }

   }

}
