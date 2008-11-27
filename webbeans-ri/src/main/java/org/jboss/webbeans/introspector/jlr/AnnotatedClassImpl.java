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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedConstructor;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedMethod;

import com.google.common.collect.ForwardingMap;

/**
 * Represents an annotated class
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class AnnotatedClassImpl<T> extends AbstractAnnotatedType<T> implements AnnotatedClass<T>
{

   /**
    * A (annotation type -> set of field abstractions with annotation) map
    */
   private static class AnnotatedFields extends ForwardingMap<Class<? extends Annotation>, Set<AnnotatedField<Object>>>
   {
      private Map<Class<? extends Annotation>, Set<AnnotatedField<Object>>> delegate;

      public AnnotatedFields()
      {
         delegate = new HashMap<Class<? extends Annotation>, Set<AnnotatedField<Object>>>();
      }

      @Override
      protected Map<Class<? extends Annotation>, Set<AnnotatedField<Object>>> delegate()
      {
         return delegate;
      }
   }

   /**
    * A (annotation type -> set of field abstractions with meta-annotation) map
    */
   private static class MetaAnnotatedFields extends ForwardingMap<Class<? extends Annotation>, Set<AnnotatedField<Object>>>
   {
      private Map<Class<? extends Annotation>, Set<AnnotatedField<Object>>> delegate;

      public MetaAnnotatedFields()
      {
         delegate = new HashMap<Class<? extends Annotation>, Set<AnnotatedField<Object>>>();
      }

      @Override
      protected Map<Class<? extends Annotation>, Set<AnnotatedField<Object>>> delegate()
      {
         return delegate;
      }
   }

   /**
    * A (annotation type -> set of method abstractions with annotation) map
    */   
   private class AnnotatedMethods extends ForwardingMap<Class<? extends Annotation>, Set<AnnotatedMethod<Object>>>
   {
      private Map<Class<? extends Annotation>, Set<AnnotatedMethod<Object>>> delegate;

      public AnnotatedMethods()
      {
         delegate = new HashMap<Class<? extends Annotation>, Set<AnnotatedMethod<Object>>>();
      }

      @Override
      protected Map<Class<? extends Annotation>, Set<AnnotatedMethod<Object>>> delegate()
      {
         return delegate;
      }
   }

   /**
    * A (annotation type -> set of constructor abstractions with annotation) map
    */
   private class AnnotatedConstructors extends ForwardingMap<Class<? extends Annotation>, Set<AnnotatedConstructor<T>>>
   {
      private Map<Class<? extends Annotation>, Set<AnnotatedConstructor<T>>> delegate;

      public AnnotatedConstructors()
      {
         delegate = new HashMap<Class<? extends Annotation>, Set<AnnotatedConstructor<T>>>();
      }

      @Override
      protected Map<Class<? extends Annotation>, Set<AnnotatedConstructor<T>>> delegate()
      {
         return delegate;
      }
   }

   /**
    * A (class list -> set of constructor abstractions with matching parameters) map
    */
   private class ConstructorsByArgument extends ForwardingMap<List<Class<?>>, AnnotatedConstructor<T>>
   {
      private Map<List<Class<?>>, AnnotatedConstructor<T>> delegate;

      public ConstructorsByArgument()
      {
         delegate = new HashMap<List<Class<?>>, AnnotatedConstructor<T>>();
      }

      @Override
      protected Map<List<Class<?>>, AnnotatedConstructor<T>> delegate()
      {
         return delegate;
      }
   }

   // The implementing class
   private Class<T> clazz;
   // The type arguments
   private Type[] actualTypeArguments;

   // The set of abstracted fields
   private Set<AnnotatedField<Object>> fields;
   // The map from annotation type to abstracted field with annotation
   private AnnotatedFields annotatedFields;
   // The map from annotation type to abstracted field with meta-annotation
   private MetaAnnotatedFields metaAnnotatedFields;

   // The set of abstracted methods
   private Set<AnnotatedMethod<Object>> methods;
   // The map from annotation type to abstracted method with annotation
   private AnnotatedMethods annotatedMethods;

   // The set of abstracted constructors
   private Set<AnnotatedConstructor<T>> constructors;
   // The map from annotation type to abstracted constructor with annotation
   private AnnotatedConstructors annotatedConstructors;
   // The map from class list to abstracted constructor
   private ConstructorsByArgument constructorsByArgumentMap;

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
   public AnnotatedClassImpl(Class<T> rawType, Type type, Annotation[] annotations)
   {
      super(buildAnnotationMap(annotations));
      this.clazz = rawType;
      if (type instanceof ParameterizedType)
      {
         actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
      }
      else
      {
         actualTypeArguments = new Type[0];
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
      if (fields == null)
      {
         initFields();
      }
      return fields;
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
      if (constructors == null)
      {
         initConstructors();
      }
      return constructors;
   }

   /**
    * Initializes the fields
    * 
    * Iterates through the type hierarchy and adds the abstracted fields to the
    * fields list
    * 
    */
   private void initFields()
   {
      this.fields = new HashSet<AnnotatedField<Object>>();
      for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass())
      {
         for (Field field : clazz.getDeclaredFields())
         {
            if (!field.isAccessible())
            {
               field.setAccessible(true);
            }
            fields.add(new AnnotatedFieldImpl<Object>(field, this));
         }
      }
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
      if (metaAnnotatedFields == null)
      {
         metaAnnotatedFields = new MetaAnnotatedFields();
      }
      if (annotatedFields == null)
      {
         initAnnotatedFields();
      }
      populateMetaAnnotatedFieldMap(metaAnnotationType, annotatedFields, metaAnnotatedFields);
      return metaAnnotatedFields.get(metaAnnotationType);
   }

   /**
    * Populates the meta annotated fields map for a meta-annotation type
    * 
    * @param <T>
    * @param metaAnnotationType The meta-annotation to examine
    * @param annotatedFields The annotated fields
    * @param metaAnnotatedFields The meta-annotated fields
    * @return The meta-annotated fields map
    */
   protected static <T extends Annotation> MetaAnnotatedFields populateMetaAnnotatedFieldMap(Class<T> metaAnnotationType, AnnotatedFields annotatedFields, MetaAnnotatedFields metaAnnotatedFields)
   {
      if (!metaAnnotatedFields.containsKey(metaAnnotationType))
      {
         Set<AnnotatedField<Object>> fields = new HashSet<AnnotatedField<Object>>();
         for (Entry<Class<? extends Annotation>, Set<AnnotatedField<Object>>> entry : annotatedFields.entrySet())
         {
            if (entry.getKey().isAnnotationPresent(metaAnnotationType))
            {
               fields.addAll(entry.getValue());
            }
         }
         metaAnnotatedFields.put(metaAnnotationType, fields);
      }
      return metaAnnotatedFields;
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
      if (annotatedFields == null)
      {
         initAnnotatedFields();
      }
      return annotatedFields.get(annotationType);
   }

   /**
    * Initializes the annotated fields map
    * 
    * If the fields set if empty, populate it first. Iterate through the fields,
    * for each field, iterate over the annotations and map the field abstraction
    * under the annotation type key.
    */
   private void initAnnotatedFields()
   {
      if (fields == null)
      {
         initFields();
      }
      annotatedFields = new AnnotatedFields();
      for (AnnotatedField<Object> field : fields)
      {
         for (Annotation annotation : field.getAnnotations())
         {
            if (!annotatedFields.containsKey(annotation))
            {
               annotatedFields.put(annotation.annotationType(), new HashSet<AnnotatedField<Object>>());
            }
            annotatedFields.get(annotation.annotationType()).add(field);
         }
      }
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
    */
   public Type[] getActualTypeArguments()
   {
      return actualTypeArguments;
   }

   /**
    * Initializes the methods
    * 
    * Iterate over the class hierarchy and for each type, add all methods
    * abstracted to the methods list
    */
   private void initMethods()
   {
      this.methods = new HashSet<AnnotatedMethod<Object>>();
      for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass())
      {
         for (Method method : clazz.getDeclaredMethods())
         {
            if (!method.isAccessible())
            {
               method.setAccessible(true);
            }
            methods.add(new AnnotatedMethodImpl<Object>(method, this));
         }
      }
   }

   /**
    * Gets the abstracted methods that have a certain annotation type present
    * 
    * If the annotated methods map is null, initialize it first
    * 
    * @param annotationType The annotation type to match
    * @return A set of matching method abstractions. Returns an empty set if no
    *         matches are found.
    */
   public Set<AnnotatedMethod<Object>> getAnnotatedMethods(Class<? extends Annotation> annotationType)
   {
      if (annotatedMethods == null)
      {
         initAnnotatedMethods();
      }

      if (!annotatedMethods.containsKey(annotationType))
      {
         return new HashSet<AnnotatedMethod<Object>>();
      }
      else
      {
         return annotatedMethods.get(annotationType);
      }
   }

   /**
    * Initializes the annotated methods
    * 
    * If the methods set is null, initialize it first. Iterate over all method
    * abstractions and for each annotation, map the method abstraction under the
    * annotation type key.
    */
   private void initAnnotatedMethods()
   {
      if (methods == null)
      {
         initMethods();
      }
      annotatedMethods = new AnnotatedMethods();
      for (AnnotatedMethod<Object> member : methods)
      {
         for (Annotation annotation : member.getAnnotations())
         {
            if (!annotatedMethods.containsKey(annotation.annotationType()))
            {
               annotatedMethods.put(annotation.annotationType(), new HashSet<AnnotatedMethod<Object>>());
            }
            annotatedMethods.get(annotation.annotationType()).add(member);
         }
      }
   }

   /**
    * Initializes the constructors set and constructors-by-argument map
    * 
    * Iterate over the constructors and for each constructor, add an abstracted
    * constructor to the constructors set and the same constructor abstraction
    * to the constructors-by-argument map under the argument-list-key.
    */
   @SuppressWarnings("unchecked")
   private void initConstructors()
   {
      this.constructors = new HashSet<AnnotatedConstructor<T>>();
      this.constructorsByArgumentMap = new ConstructorsByArgument();
      for (Constructor<?> constructor : clazz.getDeclaredConstructors())
      {
         AnnotatedConstructor<T> annotatedConstructor = new AnnotatedConstructorImpl<T>((Constructor<T>) constructor, this);
         if (!constructor.isAccessible())
         {
            constructor.setAccessible(true);
         }
         constructors.add(annotatedConstructor);
         constructorsByArgumentMap.put(Arrays.asList(constructor.getParameterTypes()), annotatedConstructor);
      }
   }

   /**
    * Gets constructors with given annotation type
    * 
    * @param annotationType The annotation type to match
    * @return A set of abstracted constructors with given annotation type. If
    *         the constructors set is empty, initialize it first .Returns an
    *         empty set if there are no matches.
    */
   public Set<AnnotatedConstructor<T>> getAnnotatedConstructors(Class<? extends Annotation> annotationType)
   {
      if (annotatedConstructors == null)
      {
         initAnnotatedConstructors();
      }

      if (!annotatedConstructors.containsKey(annotationType))
      {
         return new HashSet<AnnotatedConstructor<T>>();
      }
      else
      {
         return annotatedConstructors.get(annotationType);
      }
   }

   /**
    * Initializes the annotated constructors.
    * 
    * If the constructors set is empty, initialize it first. Iterate over all
    * constructor abstractions and for each annotation on the constructor, map
    * the constructor abstraction under the annotation key.
    */
   private void initAnnotatedConstructors()
   {
      if (constructors == null)
      {
         initConstructors();
      }
      annotatedConstructors = new AnnotatedConstructors();
      for (AnnotatedConstructor<T> constructor : constructors)
      {
         for (Annotation annotation : constructor.getAnnotations())
         {
            if (!annotatedConstructors.containsKey(annotation.annotationType()))
            {
               annotatedConstructors.put(annotation.annotationType(), new HashSet<AnnotatedConstructor<T>>());
            }
            annotatedConstructors.get(annotation.annotationType()).add(constructor);
         }
      }
   }

   public AnnotatedConstructor<T> getConstructor(List<Class<?>> arguments)
   {
      return constructorsByArgumentMap.get(arguments);
   }

}