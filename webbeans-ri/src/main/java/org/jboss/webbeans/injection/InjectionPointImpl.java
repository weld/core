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

package org.jboss.webbeans.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

import javax.context.Dependent;
import javax.inject.Initializer;
import javax.inject.Standard;
import javax.inject.manager.Bean;
import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedMember;
import org.jboss.webbeans.introspector.AnnotatedParameter;

/**
 * Represents an injection point
 * 
 * @author David Allen
 * @author Nicklas Karlsson
 */
@Standard
@Dependent
public class InjectionPointImpl implements InjectionPoint
{
   // The underlying annotated item
   private final AnnotatedItem<?, ?> annotatedItem;
   // The containing bean
   private final Bean<?> bean;

   /**
    * Creates a new injection point from an annotated item and a bean
    * 
    * @param annotatedItem The annotated item
    * @param bean The containing bean
    */
   protected InjectionPointImpl(AnnotatedItem<?, ?> annotatedItem, Bean<?> bean)
   {
      this.annotatedItem = annotatedItem;
      this.bean = bean;
   }

   /**
    * Static accessor for construction
    * 
    * @param item The annotated item
    * @param bean The containing bean
    * @return an InjectionPointImpl instance
    */
   public static InjectionPointImpl of(AnnotatedItem<?, ?> item, Bean<?> bean)
   {
      return new InjectionPointImpl(item, bean);
   }

   /**
    * Indicates if underlying item is a field
    * 
    * @return True if field, false otherwise
    */
   public boolean isField()
   {
      return getMember() instanceof Field;
   }

   /**
    * Indicates if underlying item is a method
    * 
    * @return True if method, false otherwise
    */
   public boolean isMethod()
   {
      return getMember() instanceof Method;
   }

   /**
    * Indicates if underlying item is a constructor
    * 
    * @return True if constructor, false otherwise
    */
   public boolean isConstructor()
   {
      return getMember() instanceof Constructor;
   }

   /**
    * Indicates if underlying item is an intializer
    * 
    * @return True if intializer, false otherwise
    */
   public boolean isInitializer()
   {
      return isMethod() && isAnnotationPresent(Initializer.class);
   }

   /**
    * Gets an annotation of a given type from the injection point
    * 
    * @param annotationType The meta-annotation to match
    * @return The found annotation
    * @see javax.inject.manager.InjectionPoint#getAnnotation(Class)
    */
   public <T extends Annotation> T getAnnotation(Class<T> annotationType)
   {
      return annotatedItem.getAnnotation(annotationType);
   }

   /**
    * Gets the array of annotations on the injection point
    * 
    * @return The annotations
    * @see javax.inject.manager.InjectionPoint#getAnnotations()
    */
   public Annotation[] getAnnotations()
   {
      return annotatedItem.getAnnotations().toArray(new Annotation[0]);
   }

   /**
    * Gets the containing bean
    * 
    * @return The bean
    * @see javax.inject.manager.InjectionPoint#getBean()
    */
   public Bean<?> getBean()
   {
      return this.bean;
   }

   /**
    * Gets the bindings of the injection point
    * 
    * @return The bindings
    * @see javax.inject.manager.InjectionPoint#getBindings()
    */
   public Set<Annotation> getBindings()
   {
      return annotatedItem.getBindingTypes();
   }

   /**
    * Gets the member of the injection
    * 
    * @return The underlying member
    * @see javax.inject.manager.InjectionPoint#getMember()
    */
   public Member getMember()
   {
      if (annotatedItem instanceof AnnotatedMember)
      {
         return ((AnnotatedMember<?, ?>) annotatedItem).getMember();
      }
      else if (annotatedItem instanceof AnnotatedParameter<?>)
      {
         return ((AnnotatedParameter<?>) annotatedItem).getDeclaringMember().getMember();
      }
      else
      {
         throw new IllegalArgumentException("Annotated item " + annotatedItem + " is of an unsupported type");
      }
   }

   /**
    * Gets the type of the injection point
    * 
    * @return The type
    * @see javax.inject.manager.InjectionPoint#getType
    */
   public Type getType()
   {
      return annotatedItem.getType();
   }

   /**
    * Indicates if an annotation is present on the injection point
    *
    * @param annotationType The annotation type to match
    * @return True if present, false otherwise
    * @see javax.inject.manager.InjectionPoint#isAnnotationPresent(Class)
    */
   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      return annotatedItem.isAnnotationPresent(annotationType);
   }

   @Override
   public String toString()
   {
      return annotatedItem.toString();
   }
}
