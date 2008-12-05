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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.DeploymentType;
import javax.webbeans.ScopeType;
import javax.webbeans.Stereotype;

import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.util.Reflections;
import org.jboss.webbeans.util.Strings;
import org.jboss.webbeans.util.Types;

import com.google.common.collect.ForwardingMap;

/**
 * Represents functionality common for all annotated items, mainly different
 * mappings of the annotations and meta-annotations
 * 
 * AbstractAnnotatedItem is an immutable class and therefore threadsafe
 * 
 * @author Pete Muir
 * @author Nicklas Karlsson
 * 
 * @param <T>
 * @param <S>
 * 
 * @see org.jboss.webbeans.introspector.AnnotatedItem
 */
public abstract class AbstractAnnotatedItem<T, S> implements AnnotatedItem<T, S>
{

   /**
    * Represents a mapping from a annotation type to an annotation
    * implementation
    */
   public static class AnnotationMap extends ForwardingMap<Class<? extends Annotation>, Annotation>
   {
      private final Map<Class<? extends Annotation>, Annotation> delegate;

      public AnnotationMap()
      {
         delegate = new HashMap<Class<? extends Annotation>, Annotation>();
      }

      @Override
      protected Map<Class<? extends Annotation>, Annotation> delegate()
      {
         return delegate;
      }

      /**
       * Gets a string representation of the Map
       * 
       * @return A string representation
       */
      @Override
      public String toString()
      {
         return Strings.mapToString("AnnotationMap (annotation type -> annotation): ", delegate);
      }

   }

   /**
    * Represents a mapping from a annotation (meta-annotation) to a set of
    * annotations
    * 
    */
   private static class MetaAnnotationMap extends ForwardingMap<Class<? extends Annotation>, Set<Annotation>>
   {
      private final Map<Class<? extends Annotation>, Set<Annotation>> delegate;

      public MetaAnnotationMap()
      {
         delegate = new HashMap<Class<? extends Annotation>, Set<Annotation>>();
      }

      @Override
      protected Map<Class<? extends Annotation>, Set<Annotation>> delegate()
      {
         return delegate;
      }

      /**
       * Gets the set of annotations matching the given annotation type
       * 
       * @param key The meta-annotation to match
       * @returns The set of matching annotations containing this
       *          meta-annotation
       */
      @Override
      public Set<Annotation> get(Object key)
      {
         Set<Annotation> annotations = super.get(key);
         return annotations != null ? annotations : new HashSet<Annotation>();
      }

      /**
       * Adds an annotation under the meta-annotation type key
       * 
       * @param key The meta-annotation type
       * @param value The annotation
       */
      public void put(Class<? extends Annotation> key, Annotation value)
      {
         Set<Annotation> annotations = super.get(key);
         if (annotations == null)
         {
            annotations = new HashSet<Annotation>();
            super.put(key, annotations);
         }
         annotations.add(value);
      }

      /**
       * Gets a string representation of the Map
       * 
       * @return A string representation
       */
      @Override
      public String toString()
      {
         return Strings.mapToString("MetaAnnotationMap (annotation type -> annotation set: ", delegate);
      }

   }

   // The array of default binding types
   private static final Annotation[] DEFAULT_BINDING_ARRAY = { new CurrentAnnotationLiteral() };
   // The set of default binding types
   private static final Set<Annotation> DEFAULT_BINDING = new HashSet<Annotation>(Arrays.asList(DEFAULT_BINDING_ARRAY));

   // The set of meta-annotations to map
   @SuppressWarnings("unchecked")
   private static final Set<Class<? extends Annotation>> MAPPED_METAANNOTATIONS = new HashSet<Class<? extends Annotation>>(Arrays.asList(BindingType.class, DeploymentType.class, Stereotype.class, ScopeType.class));

   // Cached string representation
   private String toString;

   /**
    * Static helper method for building annotation map from an annotated element
    * 
    * @param element The element to examine
    * @return The annotation map
    */
   protected static AnnotationMap buildAnnotationMap(AnnotatedElement element)
   {
      return buildAnnotationMap(element.getAnnotations());
   }

   /**
    * Builds the annotation map (annotation type -> annotation)
    * 
    * @param annotations The array of annotations to map
    * @return The annotation map
    */
   protected static AnnotationMap buildAnnotationMap(Annotation[] annotations)
   {
      AnnotationMap annotationMap = new AnnotationMap();
      for (Annotation annotation : annotations)
      {
         annotationMap.put(annotation.annotationType(), annotation);
      }
      return annotationMap;
   }

   /**
    * Static helper method for getting the current parameter values from a list
    * of annotated parameters.
    * 
    * @param parameters The list of annotated parameter to look up
    * @return The object array of looked up values
    * 
    */
   protected static Object[] getParameterValues(List<AnnotatedParameter<Object>> parameters)
   {
      Object[] parameterValues = new Object[parameters.size()];
      Iterator<AnnotatedParameter<Object>> iterator = parameters.iterator();
      for (int i = 0; i < parameterValues.length; i++)
      {
         parameterValues[i] = iterator.next().getValue();
      }
      return parameterValues;
   }

   // The annotation map (annotation type -> annotation) of the item
   private final AnnotationMap annotationMap;
   // The meta-annotation map (annotation type -> set of annotations containing
   // meta-annotation) of the item
   private final MetaAnnotationMap metaAnnotationMap;
   // The set of all annotations on the item
   private final Set<Annotation> annotationSet;

   /**
    * Constructor
    * 
    * Also builds the meta-annotation map. Throws a NullPointerException if
    * trying to register a null map
    * 
    * @param annotationMap A map of annotation to register
    * 
    */
   public AbstractAnnotatedItem(AnnotationMap annotationMap)
   {
      if (annotationMap == null)
      {
         throw new NullPointerException("annotationMap cannot be null");
      }
      this.annotationMap = annotationMap;
      this.annotationSet = new HashSet<Annotation>();
      this.metaAnnotationMap = new MetaAnnotationMap();
      for (Annotation annotation : annotationMap.values())
      {
         for (Annotation metaAnnotation : annotation.annotationType().getAnnotations())
         {
            // Only map meta-annotations we are interested in
            if (MAPPED_METAANNOTATIONS.contains(metaAnnotation.annotationType()))
            {
               metaAnnotationMap.put(metaAnnotation.annotationType(), annotation);
            }
         }
         annotationSet.add(annotation);
      }
   }

   /**
    * Gets the annotation for a given annotation type.
    * 
    * @param annotationType the annotation type to match
    * @return The annotation if found, null if no match was found
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#getAnnotation(Class)
    */
   @SuppressWarnings("unchecked")
   public <A extends Annotation> A getAnnotation(Class<? extends A> annotationType)
   {
      return (A) annotationMap.get(annotationType);
   }

   /**
    * Gets the set of annotations that contain a given annotation type
    * 
    * @param metaAnnotationType The meta-annotation type to match
    * @return The set of annotations containing this meta-annotation. An empty
    *         set is returned if no match is found.
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#getMetaAnnotations(Class)
    */
   public Set<Annotation> getMetaAnnotations(Class<? extends Annotation> metaAnnotationType)
   {
      return Collections.unmodifiableSet(metaAnnotationMap.get(metaAnnotationType));
   }

   /**
    * Gets (as an array) the set of annotations that contain a given annotation
    * type.
    * 
    * Populates the annotationArray if it was null
    * 
    * @param metaAnnotationType meta-annotation type to match
    * @return The array of annotations to match. An empty array is returned if
    *         no match is found.
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#getMetaAnnotationsAsArray(Class)
    */
   public Annotation[] getMetaAnnotationsAsArray(Class<? extends Annotation> metaAnnotationType)
   {
      return getMetaAnnotations(metaAnnotationType).toArray(new Annotation[0]);
   }

   /**
    * Gets all annotations on this item
    * 
    * Populates the annotationSet if it was empty
    * 
    * @return The set of annotations on this item.
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#getAnnotations()
    */
   public Set<Annotation> getAnnotations()
   {
      return Collections.unmodifiableSet(annotationSet);
   }

   /**
    * Checks if an annotation is present on the item
    * 
    * @param annotatedType The annotation type to check for
    * @return True if present, false otherwise.
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#isAnnotationPresent(Class)
    */
   public boolean isAnnotationPresent(Class<? extends Annotation> annotatedType)
   {
      return annotationMap.containsKey(annotatedType);
   }

   /**
    * Gets the annotation map
    * 
    * @return The annotation map
    */
   protected Map<Class<? extends Annotation>, Annotation> getAnnotationMap()
   {
      return Collections.unmodifiableMap(annotationMap);
   }

   /**
    * Compares two AbstractAnnotatedItems
    * 
    * @param other The other item
    * @return True if equals, false otherwise
    */
   @Override
   public boolean equals(Object other)
   {
      if (other instanceof AnnotatedItem)
      {
         AnnotatedItem<?, ?> that = (AnnotatedItem<?, ?>) other;
         return this.getAnnotations().equals(that.getAnnotations()) && this.getType().equals(that.getType());
      }
      return false;
   }

   /**
    * Checks if this item is assignable from another annotated item (through
    * type and actual type arguments)
    * 
    * @param that The other annotated item to check against
    * @return True if assignable, false otherwise
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#isAssignableFrom(AnnotatedItem)
    */
   public boolean isAssignableFrom(AnnotatedItem<?, ?> that)
   {
      return isAssignableFrom(that.getType(), that.getActualTypeArguments());
   }

   /**
    * Checks if this item is assignable from any one a set of types
    * 
    * @param types The set of types to check against
    * @return True if assignable, false otherwise
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#isAssignableFrom(Set)
    */
   public boolean isAssignableFrom(Set<Class<?>> types)
   {
      for (Class<?> type : types)
      {
         if (isAssignableFrom(type, Reflections.getActualTypeArguments(type)))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Helper method for doing actual assignability check
    * 
    * @param type The type to compare against
    * @param actualTypeArguments The type arguments
    * @return True is assignable, false otherwise
    */
   private boolean isAssignableFrom(Class<?> type, Type[] actualTypeArguments)
   {
      return Types.boxedType(getType()).isAssignableFrom(Types.boxedType(type)) && Arrays.equals(getActualTypeArguments(), actualTypeArguments);
   }

   /**
    * Gets the hash code of the actual type
    * 
    * @return The hash code
    */
   @Override
   public int hashCode()
   {
      return getType().hashCode();
   }

   /**
    * Gets a string representation of the item
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
      StringBuilder buffer = new StringBuilder();
      buffer.append("AbstractAnnotatedItem:\n");
      buffer.append(Strings.collectionToString("Annotations: ", getAnnotations()));
      buffer.append(annotationMap == null ? "" : (annotationMap.toString() + "\n"));
      buffer.append(metaAnnotationMap == null ? "" : (metaAnnotationMap.toString()) + "\n");
      toString = buffer.toString();
      return toString;
   }

   /**
    * Gets the binding types of the item
    * 
    * Looks at the meta-annotations map for annotations with binding type
    * meta-annotation. Returns default binding (current) if none specified.
    * 
    * @return A set of (binding type) annotations
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#getBindingTypes()
    */
   public Set<Annotation> getBindingTypes()
   {
      if (getMetaAnnotations(BindingType.class).size() > 0)
      {
         return Collections.unmodifiableSet(getMetaAnnotations(BindingType.class));
      }
      else
      {
         return Collections.unmodifiableSet(DEFAULT_BINDING);
      }
   }

   /**
    * Gets (as array) the binding types of the item
    * 
    * Looks at the meta-annotations map for annotations with binding type
    * meta-annotation. Returns default binding (current) if none specified.
    * 
    * @return An array of (binding type) annotations
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#getBindingTypesAsArray()
    */
   public Annotation[] getBindingTypesAsArray()
   {
      if (getMetaAnnotationsAsArray(BindingType.class).length > 0)
      {
         return getMetaAnnotationsAsArray(BindingType.class);
      }
      else
      {
         return DEFAULT_BINDING_ARRAY;
      }
   }

   /**
    * Indicates if the type is proxyable to a set of pre-defined rules
    * 
    * @return True if proxyable, false otherwise.
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#isProxyable()
    */
   public boolean isProxyable()
   {
      if (Reflections.getConstructor(getType()) == null)
      {
         return false;
      }
      else if (Reflections.isTypeOrAnyMethodFinal(getType()))
      {
         return false;
      }
      else if (Reflections.isPrimitive(getType()))
      {
         return false;
      }
      else if (Reflections.isArrayType(getType()))
      {
         return false;
      }
      else
      {
         return true;
      }
   }

   protected abstract S getDelegate();

}