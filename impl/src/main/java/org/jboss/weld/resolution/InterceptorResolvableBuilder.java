/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.logging.messages.BeanManagerMessage.DUPLICATE_INTERCEPTOR_BINDING;
import static org.jboss.weld.logging.messages.BeanManagerMessage.INTERCEPTOR_BINDINGS_EMPTY;
import static org.jboss.weld.logging.messages.BeanManagerMessage.INTERCEPTOR_RESOLUTION_WITH_NONBINDING_TYPE;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InterceptionType;

import org.jboss.weld.Container;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;

public class InterceptorResolvableBuilder extends ResolvableBuilder
{
   
   public InterceptorResolvableBuilder()
   {
      super();
   }

   public InterceptorResolvableBuilder(Type type)
   {
      super(type);
   }

   private InterceptionType interceptionType;
   
   @Override
   protected void checkQualifier(Annotation qualifier)
   {
      if (!Container.instance().services().get(MetaAnnotationStore.class).getInterceptorBindingModel(qualifier.annotationType()).isValid())
      {
         throw new IllegalArgumentException(INTERCEPTOR_RESOLUTION_WITH_NONBINDING_TYPE, qualifier);
      }
      if (qualifiers.contains(qualifier))
      {
         throw new IllegalArgumentException(DUPLICATE_INTERCEPTOR_BINDING, qualifiers);
      }
   }
   
   public InterceptorResolvableBuilder setInterceptionType(InterceptionType interceptionType)
   {
      this.interceptionType = interceptionType;
      return this;
   }
   
   @Override
   public InterceptorResolvableBuilder addQualifier(Annotation qualifier)
   {
      super.addQualifier(qualifier);
      return this;
   }
   
   @Override
   public InterceptorResolvableBuilder addQualifiers(Annotation[] qualifiers)
   {
      super.addQualifiers(qualifiers);
      return this;
   }
   
   @Override
   public InterceptorResolvableBuilder addQualifiers(Set<Annotation> qualifiers)
   {
      super.addQualifiers(qualifiers);
      return this;
   }
   
   @Override
   public InterceptorResolvableBuilder addType(Type type)
   {
      super.addType(type);
      return this;
   }
   
   @Override
   public InterceptorResolvableBuilder addTypes(Set<Type> types)
   {
      super.addTypes(types);
      return this;
   }
   
   @Override
   public InterceptorResolvableBuilder setDeclaringBean(Bean<?> declaringBean)
   {
      super.setDeclaringBean(declaringBean);
      return this;
   }
   
   @Override
   public InterceptorResolvable create()
   {
      if (qualifiers.size() == 0)
      {
         throw new IllegalArgumentException(INTERCEPTOR_BINDINGS_EMPTY);
      }
      return new InterceptorResolvableImpl(rawType, types, qualifiers, mappedQualifiers, declaringBean, interceptionType);
   }
   

   private static class InterceptorResolvableImpl extends ResolvableImpl implements InterceptorResolvable
   {
      private final InterceptionType interceptionType;

      private InterceptorResolvableImpl(Class<?> rawType, Set<Type> typeClosure, Set<Annotation> qualifiers, Map<Class<? extends Annotation>, Annotation> mappedQualifiers, Bean<?> declaringBean, InterceptionType interceptionType)
      {
         super(rawType, typeClosure, qualifiers, mappedQualifiers, declaringBean);
         this.interceptionType = interceptionType;
      }

      public InterceptionType getInterceptionType()
      {
         return interceptionType;
      }

      public int hashCode()
      {
         return 31 * super.hashCode()
               + this.getInterceptionType().hashCode();
      }

      public boolean equals(Object o)
      {
         if (o instanceof Resolvable)
         {
            Resolvable r = (Resolvable) o;
            return super.equals(r)
               && r instanceof InterceptorResolvable
               && this.getInterceptionType().equals(((InterceptorResolvable)r).getInterceptionType());
         }
         return false;
      }
   }

}
