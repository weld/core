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
import java.lang.reflect.Method;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedType;

/**
 * Represents a Class
 * 
 * @author Pete Muir
 * 
 */
public interface WBClass<T> extends WBAnnotated<T, Class<T>>, AnnotatedType<T> 
{

   /**
    * Gets all fields on the type
    * 
    * @return A set of abstracted fields
    */
   public Set<WBField<?, ?>> getWBFields();
   
   /**
    * Gets all fields on the type
    * 
    * @return A set of abstracted fields
    */
   public Set<WBMethod<?, ?>> getWBMethods();
   
   /**
    * Gets all fields on the type
    * 
    * @return A set of abstracted fields
    */
   public Set<WBMethod<?, ?>> getDeclaredWBMethods();

   /**
    * Get a field by name
    * 
    * @param <F> the expected type of the field
    * @param fieldName the field name
    * @param expectedType the expected type of the field
    * @return the field
    */
   public <F> WBField<F, ?> getDeclaredWBField(String fieldName, WBClass<F> expectedType);

   /**
    * Gets all fields which are annotated with the given annotation type on this
    * class and all super classes
    * 
    * @param annotationType The annotation to match
    * @return A set of abstracted fields with the given annotation. Returns an
    *         empty set if there are no matches
    */
   public Set<WBField<?, ?>> getAnnotatedWBFields(Class<? extends Annotation> annotationType);

   /**
    * Gets all fields which are annotated with the given annotation type on this
    * class only.
    * 
    * @param annotationType The annotation to match
    * @return A set of abstracted fields with the given annotation. Returns an
    *         empty set if there are no matches
    */
   public Set<WBField<?, T>> getDeclaredAnnotatedWBFields(Class<? extends Annotation> annotationType);

   /**
    * Gets all fields which are meta-annotated with metaAnnotationType
    * 
    * @param metaAnnotationType The meta annotation to match
    * @return A set of abstracted fields with the given meta-annotation. Returns
    *         an empty set if there are no matches
    */
   public Set<WBField<?, ?>> getMetaAnnotatedWBFields(Class<? extends Annotation> metaAnnotationType);

   /**
    * Gets all constructors which are annotated with annotationType
    * 
    * @param annotationType The annotation type to match
    * @return A set of abstracted fields with the given annotation. Returns an
    *         empty set if there are no matches
    */
   public Set<WBConstructor<T>> getAnnotatedWBConstructors(Class<? extends Annotation> annotationType);

   /**
    * Gets all constructors
    * 
    * @return A set of abstracted constructors
    */
   public Set<WBConstructor<T>> getWBConstructors();

   /**
    * Gets the no-args constructor
    * 
    * @return The no-args constructor, or null if not defined
    */
   public WBConstructor<T> getNoArgsWBConstructor();

   /**
    * Get the constructor which matches the argument list provided
    * 
    * @param parameterTypes the parameters of the constructor
    * @return the matching constructor, or null if not defined
    */
   public WBConstructor<T> getDeclaredWBConstructor(ConstructorSignature signature);

   /**
    * Gets all methods annotated with annotationType
    * 
    * @param annotationType The annotation to match
    * @return A set of abstracted methods with the given annotation. Returns an
    *         empty set if there are no matches
    */
   public Set<WBMethod<?, ?>> getAnnotatedWBMethods(Class<? extends Annotation> annotationType);

   /**
    * Gets all methods annotated with annotationType
    * 
    * @param annotationType The annotation to match
    * @return A set of abstracted methods with the given annotation. Returns an
    *         empty set if there are no matches
    */
   public Set<WBMethod<?, T>> getDeclaredAnnotatedWBMethods(Class<? extends Annotation> annotationType);

   /**
    * Find the annotated method for a given methodDescriptor
    * 
    * @param methodDescriptor
    * @return
    * 
    */
   public WBMethod<?, ?> getWBMethod(Method method);

   /**
    * Get a method by name
    * 
    * @param <M> the expected return type
    * @param signature the name of the method
    * @param expectedReturnType the expected return type
    * @return the method, or null if it doesn't exist
    */
   public <M> WBMethod<M, ?> getDeclaredWBMethod(MethodSignature signature, WBClass<M> expectedReturnType);
   
   /**
    * Get a method by name
    * 
    * @param <M> the expected return type
    * @param signature the name of the method
    * @return the method, or null if it doesn't exist
    */
   public <M> WBMethod<M, ?> getWBMethod(MethodSignature signature);

   // TODO Replace with AnnotatedMethod variant
   @Deprecated
   public WBMethod<?, ?> getDeclaredWBMethod(Method method);

   /**
    * Gets all with parameters annotated with annotationType
    * 
    * @param annotationType The annotation to match
    * @return A set of abstracted methods with the given annotation. Returns an
    *         empty set if there are no matches
    */
   public Set<WBMethod<?, ?>> getWBMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType);

   /**
    * Gets all with constructors annotated with annotationType
    * 
    * @param annotationType The annotation to match
    * @return A set of abstracted constructors with the given annotation. Returns an
    *         empty set if there are no matches
    */
   public Set<WBConstructor<?>> getWBConstructorsWithAnnotatedParameters(Class<? extends Annotation> annotationType);

   /**
    * Gets all with parameters annotated with annotationType
    * 
    * @param annotationType The annotation to match
    * @return A set of abstracted methods with the given annotation. Returns an
    *         empty set if there are no matches
    */
   public Set<WBMethod<?, T>> getDeclaredWBMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType);

   /**
    * Gets the superclass.
    * 
    * @return The abstracted superclass, null if there is no superclass
    */
   public WBClass<?> getWBSuperclass();

   /**
    * Determine if this is a non-static member class
    *
    * @return true if this is a non-static member  
    */
   public boolean isNonStaticMemberClass();

   public boolean isParameterizedType();

   public boolean isAbstract();

   public boolean isEnum();

   public <S> S cast(Object object);

   public <U> WBClass<? extends U> asWBSubclass(WBClass<U> clazz);

   /**
    * Check if this is equivalent to a java class
    * @param clazz The Java class
    * @return true if equivalent
    */
   public boolean isEquivalent(Class<?> clazz);

   public String getSimpleName();

}