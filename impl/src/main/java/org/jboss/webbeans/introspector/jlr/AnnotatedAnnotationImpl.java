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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.webbeans.introspector.AnnotatedAnnotation;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotationStore;
import org.jboss.webbeans.resources.ClassTransformer;
import org.jboss.webbeans.util.collections.multi.SetHashMultiMap;
import org.jboss.webbeans.util.collections.multi.SetMultiMap;

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

   // The annotated members map (annotation -> member with annotation)
   private final SetMultiMap<Class<? extends Annotation>, AnnotatedMethod<?>> annotatedMembers;
   // The implementation class of the annotation
   private final Class<T> clazz;
   // The set of abstracted members
   private final Set<AnnotatedMethod<?>> members;
   
   private final Map<String, AnnotatedMethod<?>> namedMembers;

   // Cached string representation
   private String toString;
   
   public static <A extends Annotation> AnnotatedAnnotation<A> of(Class<A> annotationType, ClassTransformer classTransformer)
   {
      return new AnnotatedAnnotationImpl<A>(annotationType, classTransformer);
   }

   /**
    * Constructor
    * 
    * Initializes the superclass with the built annotation map
    * 
    * @param annotationType The annotation type
    */
   protected AnnotatedAnnotationImpl(Class<T> annotationType, ClassTransformer classTransformer)
   {
      super(AnnotationStore.of(annotationType), annotationType, annotationType, classTransformer);
      this.clazz = annotationType;
      members = new HashSet<AnnotatedMethod<?>>();
      annotatedMembers = new SetHashMultiMap<Class<? extends Annotation>, AnnotatedMethod<?>>();
      this.namedMembers = new HashMap<String, AnnotatedMethod<?>>();
      for (Method member : clazz.getDeclaredMethods())
      {
         AnnotatedMethod<?> annotatedMethod = AnnotatedMethodImpl.of(member, this);
         members.add(annotatedMethod);
         for (Annotation annotation : annotatedMethod.getAnnotations())
         {
            annotatedMembers.put(annotation.annotationType(), annotatedMethod);
         }
         namedMembers.put(annotatedMethod.getName(), annotatedMethod);
      }
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
   
   public <A> AnnotatedMethod<A> getMember(String memberName, AnnotatedClass<A> expectedType)
   {
      return (AnnotatedMethod<A>) namedMembers.get(memberName);
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

   public AnnotatedAnnotation<T> wrap(Set<Annotation> annotations)
   {
      throw new UnsupportedOperationException();
   }
}
