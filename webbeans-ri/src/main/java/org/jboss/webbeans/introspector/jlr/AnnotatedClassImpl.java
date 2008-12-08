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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedConstructor;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.util.Names;
import org.jboss.webbeans.util.Strings;

import com.google.common.collect.ForwardingMap;

/**
 * Represents an annotated class
 * 
 * This class is immutable, and therefore threadsafe
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class AnnotatedClassImpl<T> extends AbstractAnnotatedType<T> implements AnnotatedClass<T>
{

   /**
    * A (annotation type -> set of field abstractions with annotation/meta
    * annotation) map
    */
   private static class AnnotatedFieldMap extends ForwardingMap<Class<? extends Annotation>, Set<AnnotatedField<Object>>>
   {
      private Map<Class<? extends Annotation>, Set<AnnotatedField<Object>>> delegate;

      public AnnotatedFieldMap()
      {
         delegate = new HashMap<Class<? extends Annotation>, Set<AnnotatedField<Object>>>();
      }

      @Override
      protected Map<Class<? extends Annotation>, Set<AnnotatedField<Object>>> delegate()
      {
         return delegate;
      }

      @Override
      public String toString()
      {
         return Strings.mapToString("AnnotatedFieldMap (annotation type -> field abstraction set): ", delegate);
      }

      @Override
      public Set<AnnotatedField<Object>> get(Object key)
      {
         Set<AnnotatedField<Object>> fields = super.get(key);
         return fields != null ? fields : new HashSet<AnnotatedField<Object>>();
      }

      public void put(Class<? extends Annotation> key, AnnotatedField<Object> value)
      {
         Set<AnnotatedField<Object>> fields = super.get(key);
         if (fields == null)
         {
            fields = new HashSet<AnnotatedField<Object>>();
            super.put(key, fields);
         }
         fields.add(value);
      }

   }

   /**
    * A (annotation type -> set of method abstractions with annotation) map
    */
   private class AnnotatedMethodMap extends ForwardingMap<Class<? extends Annotation>, Set<AnnotatedMethod<Object>>>
   {
      private Map<Class<? extends Annotation>, Set<AnnotatedMethod<Object>>> delegate;

      public AnnotatedMethodMap()
      {
         delegate = new HashMap<Class<? extends Annotation>, Set<AnnotatedMethod<Object>>>();
      }

      @Override
      protected Map<Class<? extends Annotation>, Set<AnnotatedMethod<Object>>> delegate()
      {
         return delegate;
      }

      @Override
      public String toString()
      {
         return Strings.mapToString("AnnotatedMethodMap (annotation type -> method abstraction set): ", delegate);
      }

      @Override
      public Set<AnnotatedMethod<Object>> get(Object key)
      {
         Set<AnnotatedMethod<Object>> methods = super.get(key);
         return methods != null ? methods : new HashSet<AnnotatedMethod<Object>>();
      }

      public void put(Class<? extends Annotation> key, AnnotatedMethod<Object> value)
      {
         Set<AnnotatedMethod<Object>> methods = super.get(key);
         if (methods == null)
         {
            methods = new HashSet<AnnotatedMethod<Object>>();
            super.put(key, methods);
         }
         methods.add(value);
      }

   }

   /**
    * A (annotation type -> set of constructor abstractions with annotation) map
    */
   private class AnnotatedConstructorMap extends ForwardingMap<Class<? extends Annotation>, Set<AnnotatedConstructor<T>>>
   {
      private Map<Class<? extends Annotation>, Set<AnnotatedConstructor<T>>> delegate;

      public AnnotatedConstructorMap()
      {
         delegate = new HashMap<Class<? extends Annotation>, Set<AnnotatedConstructor<T>>>();
      }

      @Override
      protected Map<Class<? extends Annotation>, Set<AnnotatedConstructor<T>>> delegate()
      {
         return delegate;
      }

      @Override
      public String toString()
      {
         return Strings.mapToString("AnnotatedConstructorMap (annotation type -> constructor abstraction set): ", delegate);
      }

      @Override
      public Set<AnnotatedConstructor<T>> get(Object key)
      {
         Set<AnnotatedConstructor<T>> constructors = super.get(key);
         return constructors != null ? constructors : new HashSet<AnnotatedConstructor<T>>();
      }

      public void add(Class<? extends Annotation> key, AnnotatedConstructor<T> value)
      {
         Set<AnnotatedConstructor<T>> constructors = super.get(key);
         if (constructors == null)
         {
            constructors = new HashSet<AnnotatedConstructor<T>>();
            super.put(key, constructors);
         }
         constructors.add(value);
      }
   }

   /**
    * A (class list -> set of constructor abstractions with matching parameters)
    * map
    */
   private class ConstructorsByArgumentMap extends ForwardingMap<List<Class<?>>, AnnotatedConstructor<T>>
   {
      private Map<List<Class<?>>, AnnotatedConstructor<T>> delegate;

      public ConstructorsByArgumentMap()
      {
         delegate = new HashMap<List<Class<?>>, AnnotatedConstructor<T>>();
      }

      @Override
      protected Map<List<Class<?>>, AnnotatedConstructor<T>> delegate()
      {
         return delegate;
      }

      @Override
      public String toString()
      {
         return Strings.mapToString("Annotation type -> constructor by arguments mappings: ", delegate);
      }
   }

   // The implementing class
   private final Class<T> clazz;
   // The type arguments
   private final Type[] actualTypeArguments;

   // The set of abstracted fields
   private final Set<AnnotatedField<Object>> fields;
   // The map from annotation type to abstracted field with annotation
   private final AnnotatedFieldMap annotatedFields;
   // The map from annotation type to abstracted field with meta-annotation
   private final AnnotatedFieldMap metaAnnotatedFields;

   // The set of abstracted methods
   private final Set<AnnotatedMethod<Object>> methods;
   // The map from annotation type to abstracted method with annotation
   private final AnnotatedMethodMap annotatedMethods;
   // The map from annotation type to method with a parameter with annotation
   private final AnnotatedMethodMap methodsByAnnotatedParameters;

   // The set of abstracted constructors
   private final Set<AnnotatedConstructor<T>> constructors;
   // The map from annotation type to abstracted constructor with annotation
   private final AnnotatedConstructorMap annotatedConstructors;
   // The map from class list to abstracted constructor
   private final ConstructorsByArgumentMap constructorsByArgumentMap;

   // Cached string representation
   private String toString;
   

   /**
    * Constructor
    * 
    * Initializes superclass with built annotation map, sets the raw type and
    * determines the actual type arguments
    * 
    * @param rawType The raw type of the class
    * @param type The type of the class
    * @param annotations The array of annotations on the class
    */
   @SuppressWarnings("unchecked")
   public AnnotatedClassImpl(Class<T> rawType, Type type, Annotation[] annotations)
   {
      super(buildAnnotationMap(annotations), rawType);
      this.clazz = rawType;
      if (type instanceof ParameterizedType)
      {
         actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
      }
      else
      {
         actualTypeArguments = new Type[0];
      }

      this.fields = new HashSet<AnnotatedField<Object>>();
      this.annotatedFields = new AnnotatedFieldMap();
      this.metaAnnotatedFields = new AnnotatedFieldMap();
      for (Class<?> c = clazz; c != Object.class && c != null; c = c.getSuperclass())
      {
         for (Field field : clazz.getDeclaredFields())
         {
            if (!field.isAccessible())
            {
               field.setAccessible(true);
            }
            AnnotatedField<Object> annotatedField = new AnnotatedFieldImpl<Object>(field, this);
            this.fields.add(annotatedField);
            for (Annotation annotation : annotatedField.getAnnotations())
            {
               this.annotatedFields.put(annotation.annotationType(), annotatedField);
               for (Annotation metaAnnotation : annotation.annotationType().getAnnotations())
               {
                  this.metaAnnotatedFields.put(metaAnnotation.annotationType(), annotatedField);
               }
            }

         }
      }

      this.constructors = new HashSet<AnnotatedConstructor<T>>();
      this.constructorsByArgumentMap = new ConstructorsByArgumentMap();
      this.annotatedConstructors = new AnnotatedConstructorMap();
      for (Constructor<?> constructor : clazz.getDeclaredConstructors())
      {
         AnnotatedConstructor<T> annotatedConstructor = new AnnotatedConstructorImpl<T>((Constructor<T>) constructor, this);
         if (!constructor.isAccessible())
         {
            constructor.setAccessible(true);
         }
         this.constructors.add(annotatedConstructor);
         this.constructorsByArgumentMap.put(Arrays.asList(constructor.getParameterTypes()), annotatedConstructor);

         for (Annotation annotation : annotatedConstructor.getAnnotations())
         {
            if (!annotatedConstructors.containsKey(annotation.annotationType()))
            {
               annotatedConstructors.put(annotation.annotationType(), new HashSet<AnnotatedConstructor<T>>());
            }
            annotatedConstructors.get(annotation.annotationType()).add(annotatedConstructor);
         }
      }

      this.methods = new HashSet<AnnotatedMethod<Object>>();
      this.annotatedMethods = new AnnotatedMethodMap();
      this.methodsByAnnotatedParameters = new AnnotatedMethodMap();
      for (Class<?> c = clazz; c != Object.class && c != null; c = c.getSuperclass())
      {
         for (Method method : clazz.getDeclaredMethods())
         {
            if (!method.isAccessible())
            {
               method.setAccessible(true);
            }

            AnnotatedMethod<Object> annotatedMethod = new AnnotatedMethodImpl<Object>(method, this);
            this.methods.add(annotatedMethod);
            for (Annotation annotation : annotatedMethod.getAnnotations())
            {
               if (!annotatedMethods.containsKey(annotation.annotationType()))
               {
                  annotatedMethods.put(annotation.annotationType(), new HashSet<AnnotatedMethod<Object>>());
               }
               annotatedMethods.get(annotation.annotationType()).add(annotatedMethod);
            }
            for (Class<? extends Annotation> annotationType : AnnotatedMethod.MAPPED_PARAMETER_ANNOTATIONS)
            {
               if (annotatedMethod.getAnnotatedParameters(annotationType).size() > 0)
               {
                  methodsByAnnotatedParameters.put(annotationType, annotatedMethod);
               }
            }
         }
      }
   }

   /**
    * Constructor
    * 
    * Calls another constructor with the class annotations array
    * 
    * @param clazz The implementing class
    */
   public AnnotatedClassImpl(Class<T> clazz)
   {
      this(clazz, clazz, clazz.getAnnotations());
   }

   /**
    * Gets the implementing class
    * 
    * @return The class
    */
   public Class<? extends T> getAnnotatedClass()
   {
      return clazz;
   }

   /**
    * Gets the delegate (class)
    * 
    * @return The class
    */
   public Class<T> getDelegate()
   {
      return clazz;
   }

   /**
    * Gets the abstracted fields of the class
    * 
    * Initializes the fields if they are null
    * 
    * @return The set of abstracted fields
    */
   public Set<AnnotatedField<Object>> getFields()
   {
      return Collections.unmodifiableSet(fields);
   }

   /**
    * Gets the abstracted constructors of the class
    * 
    * Initializes the constructors if they are null
    * 
    * @return The set of abstracted constructors
    */
   public Set<AnnotatedConstructor<T>> getConstructors()
   {
      return Collections.unmodifiableSet(constructors);
   }

   /**
    * Gets abstracted fields with requested meta-annotation type present
    * 
    * If the meta-annotations map is null, it is initializes. If the annotated
    * fields are null, it is initialized The meta-annotated field map is then
    * populated for the requested meta-annotation type and the result is
    * returned
    * 
    * @param metaAnnotationType The meta-annotation type to match
    * @return The set of abstracted fields with meta-annotation present. Returns
    *         an empty set if no matches are found.
    */
   public Set<AnnotatedField<Object>> getMetaAnnotatedFields(Class<? extends Annotation> metaAnnotationType)
   {
      return Collections.unmodifiableSet(metaAnnotatedFields.get(metaAnnotationType));
   }

   /**
    * Gets the abstracted field annotated with a specific annotation type
    * 
    * If the fields map is null, initialize it first
    * 
    * @param annotationType The annotation type to match
    * @return A set of matching abstracted fields, null if none are found.
    * 
    */
   public Set<AnnotatedField<Object>> getAnnotatedFields(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableSet(annotatedFields.get(annotationType));
   }

   /**
    * Gets the type of the class
    * 
    * @return The type
    */
   public Class<T> getType()
   {
      return clazz;
   }

   /**
    * Gets the actual type arguments
    * 
    * @return The type arguments
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedClass#getActualTypeArguments()
    */
   public Type[] getActualTypeArguments()
   {
      return actualTypeArguments;
   }

   /**
    * Gets the abstracted methods that have a certain annotation type present
    * 
    * If the annotated methods map is null, initialize it first
    * 
    * @param annotationType The annotation type to match
    * @return A set of matching method abstractions. Returns an empty set if no
    *         matches are found.
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedClass#getAnnotatedMethods(Class)
    */
   public Set<AnnotatedMethod<Object>> getAnnotatedMethods(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableSet(annotatedMethods.get(annotationType));
   }

   /**
    * Gets constructors with given annotation type
    * 
    * @param annotationType The annotation type to match
    * @return A set of abstracted constructors with given annotation type. If
    *         the constructors set is empty, initialize it first. Returns an
    *         empty set if there are no matches.
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedClass#getAnnotatedConstructors(Class)
    */
   public Set<AnnotatedConstructor<T>> getAnnotatedConstructors(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableSet(annotatedConstructors.get(annotationType));
   }

   /**
    * Gets a constructor with given arguments
    * 
    * @param arguments The arguments to match
    * @return A constructor which takes given arguments. Null is returned if
    *         there are no matches.
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedClass#getConstructor(List)
    */
   public AnnotatedConstructor<T> getConstructor(List<Class<?>> arguments)
   {
      return constructorsByArgumentMap.get(arguments);
   }
   
   public Set<AnnotatedMethod<Object>> getMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return methodsByAnnotatedParameters.get(annotationType);
   }

   /**
    * Gets a string representation of the class
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
      toString = "Annotated class " + Names.class2String(getDelegate());
      return toString;
   }
   
   public String toDetailedString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("AnnotatedConstructorImpl:\n");
      buffer.append(super.toString() + "\n");
      buffer.append(Strings.collectionToString("Actual type arguments: ", Arrays.asList(getActualTypeArguments())));
      buffer.append("Class: " + clazz.toString() + "\n");
      buffer.append(Strings.collectionToString("Fields: ", getFields()));
      buffer.append(Strings.collectionToString("Methods: ", methods));
      buffer.append(Strings.collectionToString("Constructors: ", getConstructors()));
      buffer.append(annotatedConstructors == null ? "" : (annotatedConstructors.toString() + "\n"));
      buffer.append(annotatedFields == null ? "" : (annotatedFields.toString() + "\n"));
      buffer.append(annotatedMethods == null ? "" : (annotatedMethods.toString() + "\n"));
      buffer.append(constructorsByArgumentMap == null ? "" : (constructorsByArgumentMap.toString() + "\n"));
      buffer.append(metaAnnotatedFields == null ? "" : (metaAnnotatedFields.toString() + "\n"));
      return buffer.toString();
   }

}