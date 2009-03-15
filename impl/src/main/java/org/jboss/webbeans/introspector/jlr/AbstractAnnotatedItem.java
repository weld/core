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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;

import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotationStore;
import org.jboss.webbeans.util.Proxies;
import org.jboss.webbeans.util.Reflections;
import org.jboss.webbeans.util.Types;

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
   
   interface WrappableAnnotatedItem<T, S> extends AnnotatedItem<T, S>
   {
      
      AnnotationStore getAnnotationStore();
      
   }

   public Type getType()
   {
      return getRawType();
   }
   
   // Cached string representation
   private String toString;
   private final AnnotationStore annotationStore;
   private final Class<T> rawType;
   private final Set<? extends Type> flattenedTypes;
   private final boolean proxyable;
   private final boolean _parameterizedType;

   /**
    * Constructor
    * 
    * Also builds the meta-annotation map. Throws a NullPointerException if
    * trying to register a null map
    * 
    * @param annotationMap A map of annotation to register
    * 
    */
   public AbstractAnnotatedItem(AnnotationStore annotatedItemHelper, Class<T> rawType)
   {
      this.annotationStore = annotatedItemHelper;
      this.rawType = rawType;
      this._parameterizedType = Reflections.isParameterizedType(rawType);
      this.flattenedTypes = new Reflections.HierarchyDiscovery<Type>(rawType).getFlattenedTypes();
      this.proxyable = Proxies.isTypesProxyable(flattenedTypes);
   }
   
   public AbstractAnnotatedItem(AnnotationStore annotatedItemHelper)
   {
      this.annotationStore = annotatedItemHelper;
      this.rawType = null;
      this._parameterizedType = false;
      this.flattenedTypes = null;
      this.proxyable = false;
   }
   
   public AnnotationStore getAnnotationStore()
   {
      return annotationStore;
   }

   public <A extends Annotation> A getAnnotation(Class<A> annotationType)
   {
      return getAnnotationStore().getAnnotation(annotationType);
   }

   public Set<Annotation> getMetaAnnotations(Class<? extends Annotation> metaAnnotationType)
   {
      return getAnnotationStore().getMetaAnnotations(metaAnnotationType);
   }
   
   public Set<Annotation> getDeclaredMetaAnnotations(Class<? extends Annotation> metaAnnotationType)
   {
      return getAnnotationStore().getDeclaredMetaAnnotations(metaAnnotationType);
   }

   public Annotation[] getMetaAnnotationsAsArray(Class<? extends Annotation> metaAnnotationType)
   {
      return getMetaAnnotations(metaAnnotationType).toArray(new Annotation[0]);
   }

   public Set<Annotation> getAnnotationsAsSet()
   {
      return getAnnotationStore().getAnnotations();
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
      return getAnnotationStore().isAnnotationPresent(annotatedType);
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
         return this.getAnnotationsAsSet().equals(that.getAnnotationsAsSet()) && this.getRawType().equals(that.getRawType());
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
      return isAssignableFrom(that.getRawType(), that.getActualTypeArguments());
   }

   /**
    * Checks if this item is assignable from any one a set of types
    * 
    * @param types The set of types to check against
    * @return True if assignable, false otherwise
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#isAssignableFrom(Set)
    */
   public boolean isAssignableFrom(Set<? extends Type> types)
   {
      for (Type type : types)
      {
         if (type instanceof Class)
         {
            Class<?> clazz = (Class<?>) type;
            if (isAssignableFrom(clazz, Reflections.getActualTypeArguments(clazz)))
            {
               return true;
            }
         }
         else if (type instanceof ParameterizedType)
         {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getRawType() instanceof Class)
            {
               if (isAssignableFrom((Class<?>) parameterizedType.getRawType(), parameterizedType.getActualTypeArguments()))
               {
                  return true;
               }
            }
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
      return Types.boxedType(getRawType()).isAssignableFrom(Types.boxedType(type)) && Arrays.equals(getActualTypeArguments(), actualTypeArguments);
   }

   /**
    * Gets the hash code of the actual type
    * 
    * @return The hash code
    */
   @Override
   public int hashCode()
   {
      return getRawType().hashCode();
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
      toString = "Abstract annotated item " + getName();
      return toString;
   }
   
   @Deprecated
   public Set<Annotation> getBindings()
   {
      return getAnnotationStore().getBindings();
   }

   /**
    * Gets (as array) the binding types of the item
    * 
    * Looks at the meta-annotations map for annotations with binding type
    * meta-annotation. Returns default binding (current) if none specified.
    * 
    * @return An array of (binding type) annotations
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#getBindingsAsArray()
    */
   @Deprecated
   public Annotation[] getBindingsAsArray()
   {
      return getAnnotationStore().getBindingsAsArray();
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
      return proxyable;
   }
   
   public Class<T> getRawType()
   {
      return rawType;
   }
   
   public Set<? extends Type> getFlattenedTypeHierarchy()
   {
      return flattenedTypes;
   }

   public abstract S getDelegate();
   
   public boolean isDeclaredAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      return getAnnotationStore().isDeclaredAnnotationPresent(annotationType);
   }
   
   public boolean isParameterizedType()
   {
      return _parameterizedType;
   }

}