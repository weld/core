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
package org.jboss.weld.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.NormalScope;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.spi.Annotated;
import javax.inject.Qualifier;
import javax.inject.Scope;
import javax.interceptor.InterceptorBinding;

/**
 * AnnotatedItem provides a uniform access to the annotations on an annotated
 * item defined either in Java or XML
 * 
 * @author Pete Muir
 * 
 */
public interface WeldAnnotated<T, S> extends Annotated
{

   // The set of meta-annotations to map
   @SuppressWarnings("unchecked")
   public static final Set<Class<? extends Annotation>> MAPPED_METAANNOTATIONS = new HashSet<Class<? extends Annotation>>(Arrays.asList(Qualifier.class, Stereotype.class, Scope.class, NormalScope.class, InterceptorBinding.class));

   /**
    * Gets all annotations which are annotated with the given meta annotation
    * type
    * 
    * @param The meta annotation to match
    * @return A set of matching meta-annotations. Returns an empty set if there
    *         are no matches.
    */
   public Set<Annotation> getMetaAnnotations(Class<? extends Annotation> metaAnnotationType);
   
   public Map<Class<?>, Type> getTypeClosureAsMap();

   /**
    * Gets all annotations which are declared on this annotated item with the
    * given meta annotation type
    * 
    * @param The meta annotation to match
    * @return A set of matching meta-annotations. Returns an empty set if there
    *         are no matches.
    */
   public Set<Annotation> getDeclaredMetaAnnotations(Class<? extends Annotation> metaAnnotationType);

   /**
    * Gets all annotations which are annotated with the given meta annotation
    * type
    * 
    * @param The meta annotation to match
    * @return An array of matching meta-annotations. Returns an empty array if
    *         there are no matches.
    */
   public Annotation[] getMetaAnnotationsAsArray(Class<? extends Annotation> metaAnnotationType);

   /**
    * Gets the binding types for this element
    * 
    * @returns A set of binding types present on the type. Returns an empty set
    *          if there are no matches.
    * @deprecated This reflection type should not know about JSR-299 binding
    *             types
    */
   @Deprecated
   public Set<Annotation> getQualifiers();

   /**
    * Gets the binding types for this element
    * 
    * @returns An array of binding types present on the type. Returns an empty
    *          array if there are no matches.
    * @deprecated This reflection type should not know about JSR-299 binding
    *             types
    */
   @Deprecated
   public Annotation[] getBindingsAsArray();

   /**
    * Get the type hierarchy of any interfaces implemented by this class.
    * 
    * Interface hierarchies from super classes are not included.
    * 
    * The returned types should have any type parameters resolved to their
    * actual types.
    * 
    * @return the type hierarchy
    */
   public Set<Type> getInterfaceOnlyFlattenedTypeHierarchy();

   /**
    * Indicates if an annotation type specified is present
    * 
    * @param annotationType The annotation to match
    * @return True if present, false if not
    */
   public boolean isDeclaredAnnotationPresent(Class<? extends Annotation> annotationType);

   /**
    * Gets the type of the element
    * 
    * @return The type of the element
    */
   public Class<T> getJavaClass();

   /**
    * Gets the actual type arguments for any parameterized types that this
    * AnnotatedItem represents.
    * 
    * @return An array of type arguments
    */
   public Type[] getActualTypeArguments();

   /**
    * Indicates if this AnnotatedItem represents a static element
    * 
    * @return True if static, false otherwise
    */
   public boolean isStatic();

   /**
    * Indicates if this AnnotatedItem represents a final element
    * 
    * @return True if final, false otherwise
    */
   public boolean isFinal();

   /**
    * Indicates if this AnnotatedItem can be proxyed
    * 
    * @return True if proxyable, false otherwise
    */
   public boolean isProxyable();

   /**
    * Indicates if this annotated item is public
    * 
    * @return if public, returns true
    */
   public boolean isPublic();

   public boolean isPrivate();
   
   public boolean isPackagePrivate();
   
   public Package getPackage();

   /**
    * Gets the name of this AnnotatedItem
    * 
    * If it is not possible to determine the name of the underling element, a
    * IllegalArgumentException is thrown
    * 
    * @return The name
    */
   public String getName();

   public boolean isParameterizedType();

}