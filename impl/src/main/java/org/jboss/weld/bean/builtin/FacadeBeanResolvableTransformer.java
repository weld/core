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
package org.jboss.weld.bean.builtin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Any;

import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.resolution.ForwardingResolvable;
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.resolution.ResolvableTransformer;

/**
 * AnnotatedItem transformer which can be used for FacadeBeans
 * 
 * @author Pete Muir
 *
 */
public class FacadeBeanResolvableTransformer implements ResolvableTransformer
{

   private static final Set<Annotation> bindings;
   
   static
   {
      bindings = new HashSet<Annotation>();
      bindings.add(new AnyLiteral());
   }

   private final Class<?> clazz;
   private final HashSet<Type> types;

   public FacadeBeanResolvableTransformer(Class<?> clazz)
   {
      this.clazz = clazz;
      this.types = new HashSet<Type>();
      types.add(clazz);
   }

   public Resolvable transform(final Resolvable resolvable)
   {
      if (resolvable.isAssignableTo(clazz))
      {
         return new ForwardingResolvable()
         {

            @Override
            protected Resolvable delegate()
            {
               return resolvable;
            }

            @Override
            public Set<Annotation> getQualifiers()
            {
               return Collections.unmodifiableSet(bindings);
            }

            @Override
            public Set<Type> getTypeClosure()
            {
               return Collections.unmodifiableSet(types);
            }

            @Override
            public boolean isAssignableTo(Class<?> c)
            {
               return c.isAssignableFrom(clazz);
            }

            @Override
            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
            {
               return Any.class.equals(annotationType);
            }

         };
      }
      return resolvable;
   }

}
