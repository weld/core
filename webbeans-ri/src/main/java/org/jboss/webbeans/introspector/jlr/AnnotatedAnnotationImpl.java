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

package org.jboss.webbeans.introspector.jlr;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.webbeans.introspector.AnnotatedAnnotation;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.util.Strings;

import com.google.common.collect.ForwardingMap;

/**
 * Represents an annotated annotation
 * 
 * This class is immutable and therefore threadsafe
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class AnnotatedAnnotationImpl<T extends Annotation> extends AbstractAnnotatedType<T> implements AnnotatedAnnotation<T>
{

   /**
    * A (annotation type -> set of method abstractions with annotation) map
    */
   private class AnnotatedMemberMap extends ForwardingMap<Class<? extends Annotation>, Set<AnnotatedMethod<?>>>
   {
      private Map<Class<? extends Annotation>, Set<AnnotatedMethod<?>>> delegate;

      public AnnotatedMemberMap()
      {
         delegate = new HashMap<Class<? extends Annotation>, Set<AnnotatedMethod<?>>>();
      }

      @Override
      protected Map<Class<? extends Annotation>, Set<AnnotatedMethod<?>>> delegate()
      {
         return delegate;
      }

      @Override
      public String toString()
      {
         return Strings.mapToString("AnnotatedMemberMap (annotation type -> set of member abstractions: ", delegate);
      }

      @Override
      public Set<AnnotatedMethod<?>> get(Object key)
      {
         Set<AnnotatedMethod<?>> methods = super.get(key);
         return methods != null ? methods : new HashSet<AnnotatedMethod<?>>();
      }

      public void put(Class<? extends Annotation> key, AnnotatedMethod<?> value)
      {
         Set<AnnotatedMethod<?>> members = super.get(key);
         if (members == null)
         {
            members = new HashSet<AnnotatedMethod<?>>();
            super.put(key, members);
         }
         members.add(value);
      }
   }

   // The annotated members map (annotation -> member with annotation)
   private final AnnotatedMemberMap annotatedMembers;
   // The implementation class of the annotation
   private final Class<T> clazz;
   // The set of abstracted members
   private final Set<AnnotatedMethod<?>> members;

   // Cached string representation
   private String toString;

   /**
    * Constructor
    * 
    * Initializes the superclass with the built annotation map
    * 
    * @param annotationType The annotation type
    */
   public AnnotatedAnnotationImpl(Class<T> annotationType)
   {
      super(buildAnnotationMap(annotationType), buildDeclaredAnnotationMap(annotationType), annotationType);
      this.clazz = annotationType;
      members = new HashSet<AnnotatedMethod<?>>();
      annotatedMembers = new AnnotatedMemberMap();
      for (Method member : clazz.getDeclaredMethods())
      {
         AnnotatedMethod<?> annotatedMethod = new AnnotatedMethodImpl<Object>(member, this);
         members.add(annotatedMethod);
         for (Annotation annotation : annotatedMethod.getAnnotations())
         {
            annotatedMembers.put(annotation.annotationType(), annotatedMethod);
         }
      }
   }

   /**
    * Gets the actual type arguments
    * 
    * @return The type arguments
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedAnnotation#getActualTypeArguments()
    */
   public Type[] getActualTypeArguments()
   {
      return new Type[0];
   }

   /**
    * Gets all members of the annotation
    * 
    * Initializes the members first if they are null
    * 
    * @return The set of abstracted members
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedAnnotation#getMembers()
    */
   public Set<AnnotatedMethod<?>> getMembers()
   {
      return Collections.unmodifiableSet(members);
   }

   /**
    * Gets the type of the annotation
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedAnnotation#getType()
    */
   public Class<T> getType()
   {
      return clazz;
   }

   /**
    * Returns the annotated members with a given annotation type
    * 
    * If the annotated members are null, they are initialized first.
    * 
    * @param annotationType The annotation type to match
    * @return The set of abstracted members with the given annotation type
    *         present. An empty set is returned if no matches are found
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedAnnotation#getAnnotatedMembers(Class)
    */
   public Set<AnnotatedMethod<?>> getAnnotatedMembers(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableSet(annotatedMembers.get(annotationType));
   }
   
   /**
    * Gets a string representation of the annotation
    * 
    * @return A string representation
    */
   @Override
   public String toString()
   {
      if (toString != null)
      {
         return toString;
      }
      return toString;
   }

   public Class<T> getDelegate()
   {
      return clazz;
   }

}
