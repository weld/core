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

package javax.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * The metadata for an annotated element which can be parsed by the
 * {@link BeanManager}.
 * 
 * The semantics are similar to {@link AnnotatedElement}.
 * 
 * 
 * @author Pete Muir
 * @author Clint Popetz
 * 
 */
public interface Annotated
{

   /**
    * Get the type of the annotated element
    * 
    * @return the type
    */
   public Type getBaseType();

   /**
    * Get all types the base type should be considered assignable to
    * 
    * @return a set of types the base type should be considered assignable to
    */
   public Set<Type> getTypeClosure();

   /**
    * Get the annotation instance on the annoated element for a given annotation
    * type.
    * 
    * @param <T> the type of the annotation
    * @param annotationType the class object of the annotation type
    * @return the annotation instance, or null if no annotation is present for
    *         the given annotationType
    */
   public <T extends Annotation> T getAnnotation(Class<T> annotationType);

   /**
    * Get all the annotation instances on the annotated element
    * 
    * @return the annotation instances, or an empty set if no annotations are
    *         present
    */
   public Set<Annotation> getAnnotations();

   /**
    * Determine if an annotation is present on the annotated element
    * 
    * @param annotationType the annotation type to check for
    * @return true if the annotation is present
    */
   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType);
}
