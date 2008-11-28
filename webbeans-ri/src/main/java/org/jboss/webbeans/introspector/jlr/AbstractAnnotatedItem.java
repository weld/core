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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.webbeans.BindingType;

import org.jboss.webbeans.ManagerImpl;
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
      private Map<Class<? extends Annotation>, Annotation> delegate;

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
      private Map<Class<? extends Annotation>, Set<Annotation>> delegate;

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
       * If the key is not found, an empty set is created and placed in the map
       * before returned
       * 
       * @param key The meta-annotation to match
       * @returns The set of matching annotations containing this
       *          meta-annotation
       */
      @SuppressWarnings("unchecked")
      @Override
      public Set<Annotation> get(Object key)
      {
         Set<Annotation> annotations = super.get(key);
         if (annotations == null)
         {
            annotations = new HashSet<Annotation>();
            super.put((Class<? extends Annotation>) key, annotations);
         }
         return annotations;
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
   // The array of meta-annotations to map
   private static final Annotation[] MAPPED_METAANNOTATIONS_ARRAY = {};
   // The set of meta-annotations to map
   private static final Set<Annotation> MAPPED_METAANNOTATIONS = new HashSet<Annotation>(Arrays.asList(MAPPED_METAANNOTATIONS_ARRAY));

   // The annotation map (annotation type -> annotation) of the item
   private AnnotationMap annotationMap;
   // The meta-annotation map (annotation type -> set of annotations containing
   // meta-annotation) of the item
   private MetaAnnotationMap metaAnnotationMap;
   // The set of all annotations on the item
   private Set<Annotation> annotationSet;
   // The array of all annotations on the item
   private Annotation[] annotationArray;

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
      buildMetaAnnotationMap(annotationMap);
   }

   /**
    * Build the meta-annotation map
    * 
    * Iterates through the annotationMap values (annotations) and for each
    * meta-annotation on the annotation register the annotation in the set under
    * they key of the meta-annotation type.
    * 
    * @param annotationMap The annotation map to parse
    */
   private void buildMetaAnnotationMap(AnnotationMap annotationMap)
   {
      metaAnnotationMap = new MetaAnnotationMap();
      for (Annotation annotation : annotationMap.values())
      {
         for (Annotation metaAnnotation : annotation.annotationType().getAnnotations())
         {
            // TODO: Check with Pete how to fill the array. Make annotation
            // literals for all?
            // if (MAPPED_METAANNOTATIONS.contains(metaAnnotation))
            // {
            metaAnnotationMap.get(metaAnnotation.annotationType()).add(annotation);
            // }
         }
      }
   }

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
    * @param manager The Web Beans manager
    * @return The object array of looked up values
    * 
    */
   protected static Object[] getParameterValues(List<AnnotatedParameter<Object>> parameters, ManagerImpl manager)
   {
      Object[] parameterValues = new Object[parameters.size()];
      Iterator<AnnotatedParameter<Object>> iterator = parameters.iterator();
      for (int i = 0; i < parameterValues.length; i++)
      {
         parameterValues[i] = iterator.next().getValue(manager);
      }
      return parameterValues;
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
      return metaAnnotationMap.get(metaAnnotationType);
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
      if (annotationArray == null)
      {
         annotationArray = new Annotation[0];
         annotationArray = getMetaAnnotations(metaAnnotationType).toArray(annotationArray);
      }
      return annotationArray;
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
      if (annotationSet == null)
      {
         annotationSet = new HashSet<Annotation>();
         annotationSet.addAll(annotationMap.values());
      }
      return annotationSet;
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
   protected AnnotationMap getAnnotationMap()
   {
      return annotationMap;
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
      StringBuffer buffer = new StringBuffer();
//      buffer.append("AbstractAnnotatedItem:\n");
//      buffer.append("Annotations: " + getAnnotations().size() + "\n");
//      int i = 0;
//      for (Annotation annotation : getAnnotations())
//      {
//         buffer.append(++i + " - " + annotation.toString() + "\n");
//      }
//      buffer.append(annotationMap == null ? "" : (annotationMap.toString() + "\n"));
//      buffer.append(metaAnnotationMap == null ? "" : (metaAnnotationMap.toString()) + "\n");
      return buffer.toString();
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
         return getMetaAnnotations(BindingType.class);
      }
      else
      {
         return DEFAULT_BINDING;
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

}