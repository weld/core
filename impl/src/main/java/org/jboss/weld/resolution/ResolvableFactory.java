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
package org.jboss.weld.resolution;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.InterceptionType;

import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.introspector.WeldAnnotated;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.util.collections.Arrays2;
import org.jboss.weld.util.reflection.Reflections;

public class ResolvableFactory
{

   public static Resolvable of(WeldAnnotated<?, ?> annotated)
   {
      if (annotated instanceof Resolvable)
      {
         return (Resolvable) annotated;
      }
      else
      {
         Set<Type> types = new HashSet<Type>();
         types.add(annotated.getBaseType());
         return new ResolvableImpl(types, annotated.getQualifiers(), null);
      }
   }

   public static Resolvable of(Set<Type> typeClosure, Set<Annotation> qualifiers, AbstractClassBean<?> declaringBean)
   {
      return new ResolvableImpl(typeClosure, qualifiers, declaringBean);
   }

   public static Resolvable of(Set<Type> typeClosure, Annotation[] qualifiers, AbstractClassBean<?> declaringBean)
   {
      return new ResolvableImpl(typeClosure, Arrays2.asSet(qualifiers), declaringBean);
   }

   public static InterceptorResolvable of(InterceptionType interceptionType, Annotation[] qualifiers)
   {
      return new InterceptorResolvableImpl(Arrays2.asSet(qualifiers), interceptionType );
   }

   private ResolvableFactory() {}

   private static class ResolvableImpl implements Resolvable
   {

      private final Set<Annotation> qualifiers;
      private final Map<Class<? extends Annotation>, Annotation> annotations;
      private final Set<Type> typeClosure;
      private final AbstractClassBean<?> declaringBean;

      public ResolvableImpl(Set<Type> typeClosure, Set<Annotation> qualifiers, AbstractClassBean<?> declaringBean)
      {
         this.qualifiers = qualifiers;
         if (qualifiers.size() == 0)
         {
            this.qualifiers.add(DefaultLiteral.INSTANCE);
         }
         this.annotations = new HashMap<Class<? extends Annotation>, Annotation>();
         this.typeClosure = typeClosure;
         for (Annotation annotation : qualifiers)
         {
            annotations.put(annotation.annotationType(), annotation);
         }
         this.declaringBean = declaringBean;
      }

      public Set<Annotation> getQualifiers()
      {
         return qualifiers;
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
      
      public AbstractClassBean<?> getDeclaringBean()
      {
         return declaringBean;
      }

      @Override
      public String toString()
      {
         return "Types: " + getTypeClosure() + "; Bindings: " + getQualifiers();
      }

   }

   private static class InterceptorResolvableImpl extends ResolvableImpl implements InterceptorResolvable
   {
      private final InterceptionType interceptionType;

      private InterceptorResolvableImpl(Set<Annotation> bindings, InterceptionType interceptionType)
      {
         super(Collections.singleton((Type)Object.class), bindings, null);
         this.interceptionType = interceptionType;
      }

      public InterceptionType getInterceptionType()
      {
         return interceptionType;
      }
   }

}
