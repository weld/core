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

package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import org.jboss.webbeans.bootstrap.spi.MethodDescriptor;

/**
 * Represents a Class
 * 
 * @author Pete Muir
 * 
 */
public interface AnnotatedClass<T> extends AnnotatedType<T>
{

   /**
    * Gets all fields on the type
    * 
    * @return A set of abstracted fields
    */
   public Set<AnnotatedField<Object>> getFields();

   /**
    * Gets all annotations which are annotated with the given annotation type
    * 
    * @param annotationType The annotation to match
    * @return A set of abstracted fields with the given annotation. Returns an
    *         empty set if there are no matches
    */
   public Set<AnnotatedField<Object>> getAnnotatedFields(Class<? extends Annotation> annotationType);

   /**
    * Gets all fields which are meta-annotated with metaAnnotationType
    * 
    * @param metaAnnotationType The meta annotation to match
    * @return A set of abstracted fields with the given meta-annotation. Returns
    *         an empty set if there are no matches
    */
   public Set<AnnotatedField<Object>> getMetaAnnotatedFields(Class<? extends Annotation> metaAnnotationType);

   /**
    * Gets all constructors which are annotated with annotationType
    * 
    * @param annotationType The annotation type to match
    * @return A set of abstracted fields with the given annotation. Returns an
    *         empty set if there are no matches
    */
   public Set<AnnotatedConstructor<T>> getAnnotatedConstructors(Class<? extends Annotation> annotationType);

   /**
    * Gets all constructors
    * 
    * @return A set of abstracted constructors
    */
   public Set<AnnotatedConstructor<T>> getConstructors();

   /**
    * Gets the constructor with arguments given
    * 
    * @param arguments The list of arguments to match
    * @return A set of abstracted constructors with the given arguments. Returns
    *         an empty set if there are no matches
    */
   public AnnotatedConstructor<T> getConstructor(List<Class<?>> arguments);

   /**
    * Gets all methods annotated with annotationType
    * 
    * @param annotationType The annotation to match
    * @return A set of abstracted methods with the given annotation. Returns an
    *         empty set if there are no matches
    */
   public Set<AnnotatedMethod<Object>> getAnnotatedMethods(Class<? extends Annotation> annotationType);
   
   /**
    * Find the annotated method for a given methodDescriptor
    * 
    * @param methodDescriptor
    * @return
    */
   public AnnotatedMethod<Object> getMethod(MethodDescriptor methodDescriptor);
   
   /**
    * Gets all with parameters annotated with annotationType
    * 
    * @param annotationType The annotation to match
    * @return A set of abstracted methods with the given annotation. Returns an
    *         empty set if there are no matches
    */
   public Set<AnnotatedMethod<Object>> getMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType);

   /**
    * Gets the superclass
    * 
    * @return The abstracted superclass
    */
   public AnnotatedClass<Object> getSuperclass();

}