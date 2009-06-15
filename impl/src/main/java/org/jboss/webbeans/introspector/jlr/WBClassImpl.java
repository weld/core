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
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.webbeans.introspector.WBClass;
import org.jboss.webbeans.introspector.WBConstructor;
import org.jboss.webbeans.introspector.WBField;
import org.jboss.webbeans.introspector.WBMethod;
import org.jboss.webbeans.introspector.AnnotationStore;
import org.jboss.webbeans.introspector.ConstructorSignature;
import org.jboss.webbeans.introspector.MethodSignature;
import org.jboss.webbeans.resources.ClassTransformer;
import org.jboss.webbeans.util.Names;
import org.jboss.webbeans.util.Reflections;
import org.jboss.webbeans.util.collections.multi.SetHashMultiMap;
import org.jboss.webbeans.util.collections.multi.SetMultiMap;

/**
 * Represents an annotated class
 * 
 * This class is immutable, and therefore threadsafe
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class WBClassImpl<T> extends AbstractWBType<T> implements WBClass<T>
{
   
   private static List<Class<?>> NO_ARGUMENTS = Collections.emptyList();
   
   // The set of abstracted fields
   private final Set<WBField<?>> fields;
   // The map from annotation type to abstracted field with annotation
   private final SetMultiMap<Class<? extends Annotation>, WBField<?>> annotatedFields;
   // The map from annotation type to abstracted field with meta-annotation
   private final SetMultiMap<Class<? extends Annotation>, WBField<?>> metaAnnotatedFields;
   
   // The set of abstracted fields
   private final Set<WBField<?>> declaredFields;
   private final Map<String, WBField<?>> declaredFieldsByName;
   // The map from annotation type to abstracted field with annotation
   private final SetMultiMap<Class<? extends Annotation>, WBField<?>> declaredAnnotatedFields;
   // The map from annotation type to abstracted field with meta-annotation
   private final SetMultiMap<Class<? extends Annotation>, WBField<?>> declaredMetaAnnotatedFields;
   
   // The set of abstracted methods
   private final Set<WBMethod<?>> methods;
   private final Map<MethodSignature, WBMethod<?>> declaredMethodsBySignature;
   // The map from annotation type to abstracted method with annotation
   private final SetMultiMap<Class<? extends Annotation>, WBMethod<?>> annotatedMethods;
   // The map from annotation type to method with a parameter with annotation
   private final SetMultiMap<Class<? extends Annotation>, WBMethod<?>> methodsByAnnotatedParameters;
   
   // The set of abstracted methods
   private final Set<WBMethod<?>> declaredMethods;
   // The map from annotation type to abstracted method with annotation
   private final SetMultiMap<Class<? extends Annotation>, WBMethod<?>> declaredAnnotatedMethods;
   // The map from annotation type to method with a parameter with annotation
   private final SetMultiMap<Class<? extends Annotation>, WBMethod<?>> declaredMethodsByAnnotatedParameters;
   
   // The set of abstracted constructors
   private final Set<WBConstructor<T>> constructors;
   private final Map<ConstructorSignature, WBConstructor<?>> declaredConstructorsBySignature;
   // The map from annotation type to abstracted constructor with annotation
   private final SetMultiMap<Class<? extends Annotation>, WBConstructor<T>> annotatedConstructors;
   // The map from class list to abstracted constructor
   private final Map<List<Class<?>>, WBConstructor<T>> constructorsByArgumentMap;
   
   private final SetMultiMap<Class<? extends Annotation>, WBConstructor<?>> constructorsByAnnotatedParameters;
   
   // Cached string representation
   private String toString;
   
   private final boolean _nonStaticMemberClass;
   private final boolean _abstract;
   private final boolean _enum;

   
   public static <T> WBClass<T> of(Class<T> clazz, ClassTransformer classTransformer)
   {
      return new WBClassImpl<T>(clazz, clazz, clazz.getAnnotations(), clazz.getDeclaredAnnotations(), classTransformer);
   }

   private WBClassImpl(Class<T> rawType, Type type, Annotation[] annotations, Annotation[] declaredAnnotations, ClassTransformer classTransformer)
   {
      super(AnnotationStore.of(annotations, declaredAnnotations), rawType, type, classTransformer);
      
      this.fields = new HashSet<WBField<?>>();
      this.annotatedFields = new SetHashMultiMap<Class<? extends Annotation>, WBField<?>>();
      this.metaAnnotatedFields = new SetHashMultiMap<Class<? extends Annotation>, WBField<?>>();
      this.declaredFields = new HashSet<WBField<?>>();
      this.declaredFieldsByName = new HashMap<String, WBField<?>>();
      this.declaredAnnotatedFields = new SetHashMultiMap<Class<? extends Annotation>, WBField<?>>();
      this.declaredMetaAnnotatedFields = new SetHashMultiMap<Class<? extends Annotation>, WBField<?>>();
      this._nonStaticMemberClass = Reflections.isNonStaticInnerClass(rawType);
      this._abstract = Reflections.isAbstract(rawType);
      this._enum = rawType.isEnum();
      for (Class<?> c = rawType; c != Object.class && c != null; c = c.getSuperclass())
      {
         for (Field field : c.getDeclaredFields())
         {
            if (!field.isAccessible())
            {
               field.setAccessible(true);
            }
            WBField<?> annotatedField = new WBFieldImpl<Object>(field, this);
            this.fields.add(annotatedField);
            if (c == rawType)
            {
               this.declaredFields.add(annotatedField);
               this.declaredFieldsByName.put(annotatedField.getName(), annotatedField);
            }
            for (Annotation annotation : annotatedField.getAnnotations())
            {
               this.annotatedFields.put(annotation.annotationType(), annotatedField);
               if (c == rawType)
               {
                  this.declaredAnnotatedFields.put(annotation.annotationType(), annotatedField);
               }
               for (Annotation metaAnnotation : annotation.annotationType().getAnnotations())
               {
                  this.metaAnnotatedFields.put(metaAnnotation.annotationType(), annotatedField);
                  if (c == rawType)
                  {
                     this.declaredMetaAnnotatedFields.put(metaAnnotation.annotationType(), annotatedField);
                  }
               }
            }
            
         }
      }
      
      this.constructors = new HashSet<WBConstructor<T>>();
      this.constructorsByArgumentMap = new HashMap<List<Class<?>>, WBConstructor<T>>();
      this.annotatedConstructors = new SetHashMultiMap<Class<? extends Annotation>, WBConstructor<T>>();
      this.constructorsByAnnotatedParameters = new SetHashMultiMap<Class<? extends Annotation>, WBConstructor<?>>();
      this.declaredConstructorsBySignature = new HashMap<ConstructorSignature, WBConstructor<?>>();
      for (Constructor<?> constructor : rawType.getDeclaredConstructors())
      {
         @SuppressWarnings("unchecked")
         Constructor<T> c = (Constructor<T>) constructor;
         WBConstructor<T> annotatedConstructor = WBConstructorImpl.of(c, this);
         if (!constructor.isAccessible())
         {
            constructor.setAccessible(true);
         }
         this.constructors.add(annotatedConstructor);
         this.constructorsByArgumentMap.put(Arrays.asList(constructor.getParameterTypes()), annotatedConstructor);
         
         this.declaredConstructorsBySignature.put(annotatedConstructor.getSignature(), annotatedConstructor);
         
         for (Annotation annotation : annotatedConstructor.getAnnotations())
         {
            if (!annotatedConstructors.containsKey(annotation.annotationType()))
            {
               annotatedConstructors.put(annotation.annotationType(), new HashSet<WBConstructor<T>>());
            }
            annotatedConstructors.get(annotation.annotationType()).add(annotatedConstructor);
         }
         
         for (Class<? extends Annotation> annotationType : WBConstructor.MAPPED_PARAMETER_ANNOTATIONS)
         {
            if (annotatedConstructor.getAnnotatedParameters(annotationType).size() > 0)
            {
               constructorsByAnnotatedParameters.put(annotationType, annotatedConstructor);
            }
         }
      }
      
      this.methods = new HashSet<WBMethod<?>>();
      this.annotatedMethods = new SetHashMultiMap<Class<? extends Annotation>, WBMethod<?>>();
      this.methodsByAnnotatedParameters = new SetHashMultiMap<Class<? extends Annotation>, WBMethod<?>>();
      this.declaredMethods = new HashSet<WBMethod<?>>();
      this.declaredAnnotatedMethods = new SetHashMultiMap<Class<? extends Annotation>, WBMethod<?>>();
      this.declaredMethodsByAnnotatedParameters = new SetHashMultiMap<Class<? extends Annotation>, WBMethod<?>>();
      this.declaredMethodsBySignature = new HashMap<MethodSignature, WBMethod<?>>();
      for (Class<?> c = rawType; c != Object.class && c != null; c = c.getSuperclass())
      {
         for (Method method : c.getDeclaredMethods())
         {
            if (!method.isAccessible())
            {
               method.setAccessible(true);
            }
            
            WBMethod<?> annotatedMethod = WBMethodImpl.of(method, this);
            this.methods.add(annotatedMethod);
            if (c == rawType)
            {
               this.declaredMethods.add(annotatedMethod);
               this.declaredMethodsBySignature.put(annotatedMethod.getSignature(), annotatedMethod);
            }
            for (Annotation annotation : annotatedMethod.getAnnotations())
            {
               annotatedMethods.put(annotation.annotationType(), annotatedMethod);
               if (c == rawType)
               {
                  this.declaredAnnotatedMethods.put(annotation.annotationType(), annotatedMethod);
               }
            }
            for (Class<? extends Annotation> annotationType : WBMethod.MAPPED_PARAMETER_ANNOTATIONS)
            {
               if (annotatedMethod.getAnnotatedParameters(annotationType).size() > 0)
               {
                  methodsByAnnotatedParameters.put(annotationType, annotatedMethod);
                  if (c == rawType)
                  {
                     this.declaredMethodsByAnnotatedParameters.put(annotationType, annotatedMethod);
                  }
               }
            }
         }
      }
   }
   
   /**
    * Gets the implementing class
    * 
    * @return The class
    */
   public Class<? extends T> getAnnotatedClass()
   {
      return getJavaClass();
   }
   
   /**
    * Gets the delegate (class)
    * 
    * @return The class
    */
   public Class<T> getDelegate()
   {
      return getJavaClass();
   }
   
   /**
    * Gets the abstracted fields of the class
    * 
    * Initializes the fields if they are null
    * 
    * @return The set of abstracted fields
    */
   public Set<WBField<?>> getFields()
   {
      return Collections.unmodifiableSet(fields);
   }
   
   public Set<WBField<?>> getDeclaredFields()
   {
      return Collections.unmodifiableSet(declaredFields);
   }
   
   public <F> WBField<F> getDeclaredField(String fieldName, WBClass<F> expectedType)
   {
      return (WBField<F>) declaredFieldsByName.get(fieldName);
   }
   
   public Set<WBField<?>> getDeclaredAnnotatedFields(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableSet(declaredAnnotatedFields.get(annotationType));
   }
   
   /**
    * Gets the abstracted constructors of the class
    * 
    * Initializes the constructors if they are null
    * 
    * @return The set of abstracted constructors
    */
   public Set<WBConstructor<T>> getConstructors()
   {
      return Collections.unmodifiableSet(constructors);
   }
   
   public WBConstructor<T> getDeclaredConstructor(ConstructorSignature signature)
   {
      return (WBConstructor<T>) declaredConstructorsBySignature.get(signature);
   }
   
   /**
    * Gets abstracted fields with requested meta-annotation type present
    * 
    * If the meta-annotations map is null, it is initializes. If the annotated
    * fields are null, it is initialized The meta-annotated field map is then
    * populated for the requested meta-annotation type and the result is
    * returned
    * 
    * @param metaAnnotationType
    *           The meta-annotation type to match
    * @return The set of abstracted fields with meta-annotation present. Returns
    *         an empty set if no matches are found.
    */
   public Set<WBField<?>> getMetaAnnotatedFields(Class<? extends Annotation> metaAnnotationType)
   {
      return Collections.unmodifiableSet(metaAnnotatedFields.get(metaAnnotationType));
   }
   
   /**
    * Gets the abstracted field annotated with a specific annotation type
    * 
    * If the fields map is null, initialize it first
    * 
    * @param annotationType
    *           The annotation type to match
    * @return A set of matching abstracted fields, null if none are found.
    * 
    */
   public Set<WBField<?>> getAnnotatedFields(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableSet(annotatedFields.get(annotationType));
   }
   
   public boolean isNonStaticMemberClass()
   {
      return _nonStaticMemberClass;
   }
   
   public boolean isAbstract()
   {
      return _abstract;
   }
   
   public boolean isEnum()
   {
      return _enum;
   }
   
   /**
    * Gets the abstracted methods that have a certain annotation type present
    * 
    * If the annotated methods map is null, initialize it first
    * 
    * @param annotationType
    *           The annotation type to match
    * @return A set of matching method abstractions. Returns an empty set if no
    *         matches are found.
    * 
    * @see org.jboss.webbeans.introspector.WBClass#getAnnotatedMethods(Class)
    */
   public Set<WBMethod<?>> getAnnotatedMethods(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableSet(annotatedMethods.get(annotationType));
   }
   
   public Set<WBMethod<?>> getDeclaredAnnotatedMethods(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableSet(declaredAnnotatedMethods.get(annotationType));
   }
   
   /**
    * Gets constructors with given annotation type
    * 
    * @param annotationType
    *           The annotation type to match
    * @return A set of abstracted constructors with given annotation type. If
    *         the constructors set is empty, initialize it first. Returns an
    *         empty set if there are no matches.
    * 
    * @see org.jboss.webbeans.introspector.WBClass#getAnnotatedConstructors(Class)
    */
   public Set<WBConstructor<T>> getAnnotatedConstructors(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableSet(annotatedConstructors.get(annotationType));
   }
   
   public WBConstructor<T> getNoArgsConstructor()
   {
      return constructorsByArgumentMap.get(NO_ARGUMENTS);
   }
   
   public Set<WBMethod<?>> getMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableSet(methodsByAnnotatedParameters.get(annotationType));
   }
   
   public Set<WBConstructor<?>> getConstructorsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableSet(constructorsByAnnotatedParameters.get(annotationType));
   }
   
   public Set<WBMethod<?>> getDeclaredMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableSet(declaredMethodsByAnnotatedParameters.get(annotationType));
   }
   
   public WBMethod<?> getMethod(Method methodDescriptor)
   {
      // TODO Should be cached
      for (WBMethod<?> annotatedMethod : methods)
      {
         if (annotatedMethod.getName().equals(methodDescriptor.getName()) && Arrays.equals(annotatedMethod.getParameterTypesAsArray(), methodDescriptor.getParameterTypes()))
         {
            return annotatedMethod;
         }
      }
      return null;
   }
   
   public WBMethod<?> getDeclaredMethod(Method method)
   {
      // TODO Should be cached
      for (WBMethod<?> annotatedMethod : declaredMethods)
      {
         if (annotatedMethod.getName().equals(method.getName()) && Arrays.equals(annotatedMethod.getParameterTypesAsArray(), method.getParameterTypes()))
         {
            return annotatedMethod;
         }
      }
      return null;
   }
   
   @SuppressWarnings("unchecked")
   public <M> WBMethod<M> getDeclaredMethod(MethodSignature signature, WBClass<M> expectedReturnType)
   {
      return (WBMethod<M>) declaredMethodsBySignature.get(signature);
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
      toString = "Annotated class " + Names.classToString(getDelegate());
      return toString;
   }
   
   @SuppressWarnings("unchecked")
   public <U> WBClass<? extends U> asSubclass(WBClass<U> clazz)
   {
      return (WBClass<? extends U>) this;
   }
   
   @SuppressWarnings("unchecked")
   public T cast(Object object)
   {
      return (T) object;
   }
   
}