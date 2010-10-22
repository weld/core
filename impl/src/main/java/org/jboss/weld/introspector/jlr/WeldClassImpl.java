/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.introspector.jlr;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.introspector.ConstructorSignature;
import org.jboss.weld.introspector.ExternalAnnotatedType;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldConstructor;
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.collections.ArraySet;
import org.jboss.weld.util.collections.ArraySetMultimap;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.SecureReflections;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Sets;

/**
 * Represents an annotated class
 * 
 * This class is immutable, and therefore threadsafe
 * 
 * @author Pete Muir
 * @author David Allen
 * 
 * @param <T> the type of the class
 */
public class WeldClassImpl<T> extends AbstractWeldAnnotated<T, Class<T>> implements WeldClass<T>
{

   private static <T> void mapConstructorAnnotations(ArrayListMultimap<Class<? extends Annotation>, WeldConstructor<T>> annotatedConstructors, WeldConstructor<T> annotatedConstructor)
   {
      for (Annotation annotation : annotatedConstructor.getAnnotations())
      {
         if (!annotatedConstructors.containsKey(annotation.annotationType()))
         {
            annotatedConstructors.putAll(annotation.annotationType(), new HashSet<WeldConstructor<T>>());
         }
         annotatedConstructors.get(annotation.annotationType()).add(annotatedConstructor);
      }
   }

   // Class attributes
   private final WeldClass<?> superclass;

   // The set of abstracted fields
   private final Set<WeldField<?, ?>> fields;
   // The map from annotation type to abstracted field with annotation
   private final ArrayListMultimap<Class<? extends Annotation>, WeldField<?, ?>> annotatedFields;

   // The set of abstracted fields
   private final ArraySet<WeldField<?, ?>> declaredFields;
   private final Map<String, WeldField<?, ?>> declaredFieldsByName;
   // The map from annotation type to abstracted field with annotation
   private final ArrayListMultimap<Class<? extends Annotation>, WeldField<?, ? super T>> declaredAnnotatedFields;
   // The map from annotation type to abstracted field with meta-annotation
   private final ArrayListMultimap<Class<? extends Annotation>, WeldField<?, ?>> declaredMetaAnnotatedFields;

   // The set of abstracted methods
   private final Set<WeldMethod<?, ?>> methods;
   private final Map<MethodSignature, WeldMethod<?, ?>> declaredMethodsBySignature;
   private final Map<MethodSignature, WeldMethod<?, ?>> methodsBySignature;
   // The map from annotation type to abstracted method with annotation
   private final ArrayListMultimap<Class<? extends Annotation>, WeldMethod<?, ?>> annotatedMethods;

   // The set of abstracted methods
   private final ArraySet<WeldMethod<?, ?>> declaredMethods;
   // The map from annotation type to abstracted method with annotation
   private final ArrayListMultimap<Class<? extends Annotation>, WeldMethod<?, ? super T>> declaredAnnotatedMethods;
   // The map from annotation type to method with a parameter with annotation
   private final ArrayListMultimap<Class<? extends Annotation>, WeldMethod<?, ? super T>> declaredMethodsByAnnotatedParameters;

   // The set of abstracted constructors
   private final ArrayList<AnnotatedConstructor<T>> constructors;
   private final Map<ConstructorSignature, WeldConstructor<?>> declaredConstructorsBySignature;
   // The map from annotation type to abstracted constructor with annotation
   private final ArrayListMultimap<Class<? extends Annotation>, WeldConstructor<T>> annotatedConstructors;
   // The map from class list to abstracted constructor
   private final Map<List<Class<?>>, WeldConstructor<T>> constructorsByArgumentMap;

   // The meta-annotation map (annotation type -> set of annotations containing
   // meta-annotation) of the item
   private final ArraySetMultimap<Class<? extends Annotation>, Annotation> declaredMetaAnnotationMap;

   private final boolean discovered;

   public static <T> WeldClass<T> of(Class<T> clazz, ClassTransformer classTransformer)
   {
      return new WeldClassImpl<T>(clazz, clazz, null, new HierarchyDiscovery(clazz).getTypeClosure(), buildAnnotationMap(clazz.getAnnotations()), buildAnnotationMap(clazz.getDeclaredAnnotations()), classTransformer);
   }

   public static <T> WeldClass<T> of(AnnotatedType<T> annotatedType, ClassTransformer classTransformer)
   {
      return new WeldClassImpl<T>(annotatedType.getJavaClass(), annotatedType.getBaseType(), annotatedType, annotatedType.getTypeClosure(), buildAnnotationMap(annotatedType.getAnnotations()), buildAnnotationMap(annotatedType.getAnnotations()), classTransformer);
   }

   public static <T> WeldClass<T> of(Class<T> rawType, Type type, ClassTransformer classTransformer)
   {
      return new WeldClassImpl<T>(rawType, type, null, new HierarchyDiscovery(type).getTypeClosure(), buildAnnotationMap(rawType.getAnnotations()), buildAnnotationMap(rawType.getDeclaredAnnotations()), classTransformer);
   }

   @SuppressWarnings("unchecked")
   protected WeldClassImpl(Class<T> rawType, Type type, AnnotatedType<T> annotatedType, Set<Type> typeClosure, Map<Class<? extends Annotation>, Annotation> annotationMap, Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap, ClassTransformer classTransformer)
   {
      super(annotationMap, declaredAnnotationMap, classTransformer, rawType, type, typeClosure);

      if (annotatedType instanceof ExternalAnnotatedType)
      {
         discovered = false;
      }
      else
      {
         discovered = true;
      }

      if (rawType.getSuperclass() != null)
      {
         this.superclass = classTransformer.loadClass(rawType.getSuperclass());
      }
      else
      {
         this.superclass = null;
      }

      // Assign class field information
      this.declaredFieldsByName = new HashMap<String, WeldField<?, ?>>();
      this.declaredAnnotatedFields = ArrayListMultimap.<Class<? extends Annotation>, WeldField<?, ? super T>> create();
      this.declaredMetaAnnotatedFields = ArrayListMultimap.<Class<? extends Annotation>, WeldField<?, ?>> create();

      Set<WeldField<?, ?>> fieldsTemp = null;
      ArrayList<WeldField<?, ?>> declaredFieldsTemp = new ArrayList<WeldField<?, ?>>();
      if (annotatedType == null)
      {
         this.annotatedFields = null;
         if (rawType != Object.class) {
            for (Field field : SecureReflections.getDeclaredFields(rawType))
            {
               WeldField<?, T> annotatedField = WeldFieldImpl.of(field, this.<T> getDeclaringWeldClass(field, classTransformer), classTransformer);
               declaredFieldsTemp.add(annotatedField);
               this.declaredFieldsByName.put(annotatedField.getName(), annotatedField);
               for (Annotation annotation : annotatedField.getAnnotations())
               {
                  this.declaredAnnotatedFields.put(annotation.annotationType(), annotatedField);
                  for (Annotation metaAnnotation : annotation.annotationType().getAnnotations())
                  {
                     this.declaredMetaAnnotatedFields.put(metaAnnotation.annotationType(), annotatedField);
                  }
               }
            }
            fieldsTemp = new ArraySet<WeldField<?, ?>>(declaredFieldsTemp).trimToSize();
            if ((superclass != null) && (superclass.getJavaClass() != Object.class))
            {
               fieldsTemp = Sets.union(fieldsTemp, (Set<WeldField<?, ?>>) ((Set<?>)superclass.getFields()));
            }
         }
         this.declaredFields = new ArraySet<WeldField<?, ?>>(declaredFieldsTemp);
      }
      else
      {
         this.annotatedFields = ArrayListMultimap.<Class<? extends Annotation>, WeldField<?, ?>> create();
         fieldsTemp = new HashSet<WeldField<?, ?>>();
         for (AnnotatedField<? super T> annotatedField : annotatedType.getFields())
         {
            WeldField<?, ? super T> weldField = WeldFieldImpl.of(annotatedField, this, classTransformer);
            fieldsTemp.add(weldField);
            if (annotatedField.getDeclaringType().getJavaClass() == rawType)
            {
               declaredFieldsTemp.add(weldField);
               this.declaredFieldsByName.put(weldField.getName(), weldField);
            }
            for (Annotation annotation : weldField.getAnnotations())
            {
               this.annotatedFields.put(annotation.annotationType(), weldField);
               if (annotatedField.getDeclaringType().getJavaClass() == rawType)
               {
                  this.declaredAnnotatedFields.put(annotation.annotationType(), weldField);
                  for (Annotation metaAnnotation : annotation.annotationType().getAnnotations())
                  {
                     this.declaredMetaAnnotatedFields.put(metaAnnotation.annotationType(), weldField);
                  }
               }
            }
         }
         this.declaredFields = new ArraySet<WeldField<?, ?>>(declaredFieldsTemp);
         fieldsTemp = new ArraySet<WeldField<?, ?>>(fieldsTemp).trimToSize();
         this.annotatedFields.trimToSize();
      }
      this.fields = fieldsTemp;
      this.declaredFields.trimToSize();
      this.declaredAnnotatedFields.trimToSize();
      this.declaredMetaAnnotatedFields.trimToSize();

      // Assign constructor information
      this.constructors = new ArrayList<AnnotatedConstructor<T>>();
      this.constructorsByArgumentMap = new HashMap<List<Class<?>>, WeldConstructor<T>>();
      this.annotatedConstructors = ArrayListMultimap.<Class<? extends Annotation>, WeldConstructor<T>> create();

      this.declaredConstructorsBySignature = new HashMap<ConstructorSignature, WeldConstructor<?>>();
      if (annotatedType == null)
      {
         for (Constructor<?> constructor : SecureReflections.getDeclaredConstructors(rawType))
         {
            Constructor<T> c = (Constructor<T>) constructor;

            WeldConstructor<T> annotatedConstructor = WeldConstructorImpl.of(c, this.<T> getDeclaringWeldClass(c, classTransformer), classTransformer);
            this.constructors.add(annotatedConstructor);
            this.constructorsByArgumentMap.put(Arrays.asList(constructor.getParameterTypes()), annotatedConstructor);
            this.declaredConstructorsBySignature.put(annotatedConstructor.getSignature(), annotatedConstructor);
            mapConstructorAnnotations(annotatedConstructors, annotatedConstructor);
         }
      }
      else
      {
         for (AnnotatedConstructor<T> constructor : annotatedType.getConstructors())
         {
            WeldConstructor<T> weldConstructor = WeldConstructorImpl.of(constructor, this, classTransformer);

            this.constructors.add(weldConstructor);

            List<Class<?>> parameterTypes = new ArrayList<Class<?>>();
            for (AnnotatedParameter<T> parameter : constructor.getParameters())
            {
               parameterTypes.add(Reflections.getRawType(parameter.getBaseType()));
            }
            this.constructorsByArgumentMap.put(parameterTypes, weldConstructor);
            this.declaredConstructorsBySignature.put(weldConstructor.getSignature(), weldConstructor);
            mapConstructorAnnotations(annotatedConstructors, weldConstructor);
         }
      }
      this.constructors.trimToSize();
      this.annotatedConstructors.trimToSize();

      // Assign method information
      this.declaredAnnotatedMethods = ArrayListMultimap.<Class<? extends Annotation>, WeldMethod<?, ? super T>> create();
      this.declaredMethodsByAnnotatedParameters = ArrayListMultimap.<Class<? extends Annotation>, WeldMethod<?, ? super T>> create();
      this.declaredMethodsBySignature = new HashMap<MethodSignature, WeldMethod<?, ?>>();

      Set<WeldMethod<?, ?>> methodsTemp = null;
      ArrayList<WeldMethod<?, ?>> declaredMethodsTemp = new ArrayList<WeldMethod<?, ?>>();
      if (annotatedType == null)
      {
         this.annotatedMethods = null;
         this.methodsBySignature = null;
         if (rawType != Object.class)
         {
            for (Method method : SecureReflections.getDeclaredMethods(rawType))
            {
               WeldMethod<?, T> weldMethod  = WeldMethodImpl.of(method, this.<T> getDeclaringWeldClass(method, classTransformer), classTransformer);
               declaredMethodsTemp.add(weldMethod);
               this.declaredMethodsBySignature.put(weldMethod.getSignature(), weldMethod);
               for (Annotation annotation : weldMethod.getAnnotations())
               {
                  this.declaredAnnotatedMethods.put(annotation.annotationType(), weldMethod);
               }
               for (Class<? extends Annotation> annotationType : WeldMethod.MAPPED_PARAMETER_ANNOTATIONS)
               {
                  if (weldMethod.getWeldParameters(annotationType).size() > 0)
                  {
                     this.declaredMethodsByAnnotatedParameters.put(annotationType, weldMethod);
                  }
               }
            }
            methodsTemp = new ArraySet<WeldMethod<?, ?>>(declaredMethodsTemp).trimToSize();
            if ((superclass != null) && (superclass.getJavaClass() != Object.class))
            {
               methodsTemp = Sets.union(methodsTemp, (Set<WeldMethod<?, ?>>) superclass.getDeclaredWeldMethods());
            }
         }
         this.declaredMethods = new ArraySet<WeldMethod<?, ?>>(declaredMethodsTemp);
      }
      else
      {
         this.annotatedMethods = ArrayListMultimap.<Class<? extends Annotation>, WeldMethod<?, ?>> create();
         this.methodsBySignature = new HashMap<MethodSignature, WeldMethod<?, ?>>();
         methodsTemp = new HashSet<WeldMethod<?, ?>>();
         for (AnnotatedMethod<? super T> method : annotatedType.getMethods())
         {
            WeldMethod<?, ? super T> weldMethod = WeldMethodImpl.of(method, this, classTransformer);
            methodsTemp.add(weldMethod);
            this.methodsBySignature.put(weldMethod.getSignature(), weldMethod);
            if (method.getDeclaringType().getJavaClass() == rawType)
            {
               declaredMethodsTemp.add(weldMethod);
               this.declaredMethodsBySignature.put(weldMethod.getSignature(), weldMethod);
            }
            for (Annotation annotation : weldMethod.getAnnotations())
            {
               annotatedMethods.put(annotation.annotationType(), weldMethod);
               if (method.getDeclaringType().getJavaClass() == rawType)
               {
                  this.declaredAnnotatedMethods.put(annotation.annotationType(), weldMethod);
               }
            }
            for (Class<? extends Annotation> annotationType : WeldMethod.MAPPED_PARAMETER_ANNOTATIONS)
            {
               if (weldMethod.getWeldParameters(annotationType).size() > 0)
               {
                  if (method.getDeclaringType().getJavaClass() == rawType)
                  {
                     this.declaredMethodsByAnnotatedParameters.put(annotationType, weldMethod);
                  }
               }
            }
         }
         this.declaredMethods = new ArraySet<WeldMethod<?, ?>>(declaredMethodsTemp);
         methodsTemp = new ArraySet<WeldMethod<?, ?>>(methodsTemp).trimToSize();
         this.annotatedMethods.trimToSize();
      }
      this.methods = methodsTemp;
      this.declaredMethods.trimToSize();
      this.declaredAnnotatedMethods.trimToSize();
      this.declaredMethodsByAnnotatedParameters.trimToSize();

      this.declaredMetaAnnotationMap = new ArraySetMultimap<Class<? extends Annotation>, Annotation>();
      for (Annotation declaredAnnotation : declaredAnnotationMap.values())
      {
         addMetaAnnotations(declaredMetaAnnotationMap, declaredAnnotation, declaredAnnotation.annotationType().getAnnotations(), true);
         addMetaAnnotations(declaredMetaAnnotationMap, declaredAnnotation, classTransformer.getTypeStore().get(declaredAnnotation.annotationType()), true);
         this.declaredMetaAnnotationMap.putSingleElement(declaredAnnotation.annotationType(), declaredAnnotation);
      }
      declaredMetaAnnotationMap.trimToSize();
   }

   @SuppressWarnings("unchecked")
   private <X> WeldClass<X> getDeclaringWeldClass(Member member, ClassTransformer transformer)
   {
      if (member.getDeclaringClass().equals(getJavaClass()))
      {
         return (WeldClass<X>) this;
      }
      else
      {
         return transformer.loadClass((Class<X>) member.getDeclaringClass());
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
   @Override
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
   public Collection<WeldField<?, ?>> getWeldFields()
   {
      return Collections.unmodifiableCollection(fields);
   }

   public Collection<WeldField<?, ?>> getDeclaredFields()
   {
      return Collections.unmodifiableCollection(declaredFields);
   }

   @SuppressWarnings("unchecked")
   public <F> WeldField<F, ?> getDeclaredWeldField(String fieldName)
   {
      return (WeldField<F, ?>) declaredFieldsByName.get(fieldName);
   }

   public Collection<WeldField<?, ? super T>> getDeclaredWeldFields(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableCollection(declaredAnnotatedFields.get(annotationType));
   }

   @SuppressWarnings("unchecked")
   public WeldConstructor<T> getDeclaredWeldConstructor(ConstructorSignature signature)
   {
      return (WeldConstructor<T>) declaredConstructorsBySignature.get(signature);
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
   public Collection<WeldField<?, ?>> getWeldFields(Class<? extends Annotation> annotationType)
   {
      if (annotatedFields == null)
      {
         // Build collection from class hierarchy
         ArrayList<WeldField<?, ?>> aggregatedFields = new ArrayList<WeldField<?, ?>>(this.declaredAnnotatedFields.get(annotationType));
         if ((superclass != null) && (superclass.getJavaClass() != Object.class))
         {
            aggregatedFields.addAll(superclass.getWeldFields(annotationType));
         }
         return Collections.unmodifiableCollection(aggregatedFields);
      }
      else
      {
         // Return results collected directly from AnnotatedType
         return Collections.unmodifiableCollection(annotatedFields.get(annotationType));
      }
   }

   public boolean isLocalClass()
   {
      return getJavaClass().isLocalClass();
   }

   public boolean isAnonymousClass()
   {
      return getJavaClass().isAnonymousClass();
   }

   public boolean isMemberClass()
   {
      return getJavaClass().isMemberClass();
   }

   public boolean isAbstract()
   {
      return Modifier.isAbstract(getJavaClass().getModifiers());
   }

   public boolean isEnum()
   {
      return getJavaClass().isEnum();
   }

   public boolean isSerializable()
   {
      return Reflections.isSerializable(getJavaClass());
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
    * @see org.jboss.weld.introspector.WeldClass#getWeldMethods(Class)
    */
   public Collection<WeldMethod<?, ?>> getWeldMethods(Class<? extends Annotation> annotationType)
   {
      if (annotatedMethods == null)
      {
         ArrayList<WeldMethod<?, ?>> aggregateMethods = new ArrayList<WeldMethod<?, ?>>(this.declaredAnnotatedMethods.get(annotationType));
         if ((superclass != null) && (superclass.getJavaClass() != Object.class))
         {
            aggregateMethods.addAll(superclass.getDeclaredWeldMethods(annotationType));
         }
         return Collections.unmodifiableCollection(aggregateMethods);
      }
      else
      {
         return Collections.unmodifiableCollection(annotatedMethods.get(annotationType));
      }
   }

   public Collection<WeldMethod<?, ? super T>> getDeclaredWeldMethods(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableCollection(declaredAnnotatedMethods.get(annotationType));
   }

   /**
    * Gets constructors with given annotation type
    * 
    * @param annotationType The annotation type to match
    * @return A set of abstracted constructors with given annotation type. If
    *         the constructors set is empty, initialize it first. Returns an
    *         empty set if there are no matches.
    * 
    * @see org.jboss.weld.introspector.WeldClass#getWeldConstructors(Class)
    */
   public Collection<WeldConstructor<T>> getWeldConstructors(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableCollection(annotatedConstructors.get(annotationType));
   }

   public WeldConstructor<T> getNoArgsWeldConstructor()
   {
      return constructorsByArgumentMap.get(Collections.emptyList());
   }

   public Collection<WeldMethod<?, ? super T>> getDeclaredWeldMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableCollection(declaredMethodsByAnnotatedParameters.get(annotationType));
   }

   public Collection<WeldMethod<?, ? super T>> getWeldMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      // TODO should be cached
      ArrayList<WeldMethod<?, ? super T>> methods = new ArrayList<WeldMethod<?, ? super T>>();
      for (WeldMethod<?, ?> method : getWeldMethods())
      {
         if (!method.getWeldParameters(annotationType).isEmpty())
         {
            methods.add((WeldMethod<?, ? super T>) method);
         }
      }
      return Collections.unmodifiableCollection(methods);
   }

   public WeldMethod<?, ?> getWeldMethod(Method methodDescriptor)
   {
      // TODO Should be cached
      for (WeldMethod<?, ?> annotatedMethod : getWeldMethods())
      {
         if (annotatedMethod.getName().equals(methodDescriptor.getName()) && Arrays.equals(annotatedMethod.getParameterTypesAsArray(), methodDescriptor.getParameterTypes()))
         {
            return annotatedMethod;
         }
      }
      return null;
   }

   public Collection<WeldMethod<?, ?>> getWeldMethods()
   {
      return Collections.unmodifiableSet(methods);
   }

   public WeldMethod<?, ?> getDeclaredWeldMethod(Method method)
   {
      // TODO Should be cached
      for (WeldMethod<?, ?> annotatedMethod : declaredMethods)
      {
         if (annotatedMethod.getName().equals(method.getName()) && Arrays.equals(annotatedMethod.getParameterTypesAsArray(), method.getParameterTypes()))
         {
            return annotatedMethod;
         }
      }
      return null;
   }

   public Collection<WeldMethod<?, ?>> getDeclaredWeldMethods()
   {
      return Collections.unmodifiableSet(declaredMethods);
   }

   @SuppressWarnings("unchecked")
   public <M> WeldMethod<M, ?> getDeclaredWeldMethod(MethodSignature signature)
   {
      return (WeldMethod<M, ?>) declaredMethodsBySignature.get(signature);
   }

   @SuppressWarnings("unchecked")
   public <M> WeldMethod<M, ?> getWeldMethod(MethodSignature signature)
   {
      if (methodsBySignature == null)
      {
         WeldMethod<M, ?> method = (WeldMethod<M, ?>) declaredMethodsBySignature.get(signature);
         if ((method == null) && (superclass != null) && (superclass.getJavaClass() != Object.class))
         {
            method = superclass.getWeldMethod(signature);
         }
         return method;
      }
      else
      {
         return (WeldMethod<M, ?>) methodsBySignature.get(signature);
      }
   }

   /**
    * Gets a string representation of the class
    * 
    * @return A string representation
    */
   @Override
   public String toString()
   {
      return Formats.formatModifiers(getJavaClass().getModifiers()) + Formats.formatAnnotations(getAnnotations()) + " class " + getName() + Formats.formatActualTypeArguments(getActualTypeArguments());
   }

   public String getSimpleName()
   {
      return getJavaClass().getSimpleName();
   }

   /**
    * Indicates if the type is static
    * 
    * @return True if static, false otherwise
    * 
    * @see org.jboss.weld.introspector.WeldAnnotated#isStatic()
    */
   public boolean isStatic()
   {
      return Reflections.isStatic(getDelegate());
   }

   /**
    * Indicates if the type if final
    * 
    * @return True if final, false otherwise
    * 
    * @see org.jboss.weld.introspector.WeldAnnotated#isFinal()
    */
   public boolean isFinal()
   {
      return Reflections.isFinal(getDelegate());
   }

   public boolean isPublic()
   {
      return Modifier.isFinal(getJavaClass().getModifiers());
   }

   public boolean isGeneric()
   {
      return getJavaClass().getTypeParameters().length > 0;
   }

   /**
    * Gets the name of the type
    * 
    * @returns The name
    * 
    * @see org.jboss.weld.introspector.WeldAnnotated#getName()
    */
   public String getName()
   {
      return getJavaClass().getName();
   }

   /**
    * Gets the superclass abstraction of the type
    * 
    * @return The superclass abstraction
    */
   public WeldClass<?> getWeldSuperclass()
   {
      return superclass;
   }

   public boolean isEquivalent(Class<?> clazz)
   {
      return getDelegate().equals(clazz);
   }

   public boolean isPrivate()
   {
      return Modifier.isPrivate(getJavaClass().getModifiers());
   }

   public boolean isPackagePrivate()
   {
      return Reflections.isPackagePrivate(getJavaClass().getModifiers());
   }

   public Package getPackage()
   {
      return getJavaClass().getPackage();
   }

   @SuppressWarnings("unchecked")
   public <U> WeldClass<? extends U> asWeldSubclass(WeldClass<U> clazz)
   {
      return (WeldClass<? extends U>) this;
   }

   @SuppressWarnings("unchecked")
   public <S> S cast(Object object)
   {
      return (S) object;
   }

   public Set<AnnotatedConstructor<T>> getConstructors()
   {
      return Collections.unmodifiableSet(new ArraySet<AnnotatedConstructor<T>>(constructors));
   }

   @SuppressWarnings("unchecked")
   public Set<AnnotatedField<? super T>> getFields()
   {
      return (Set<AnnotatedField<? super T>>) ((Collection<?>)fields);
   }

   @SuppressWarnings("unchecked")
   public Set<AnnotatedMethod<? super T>> getMethods()
   {
      return (Set<AnnotatedMethod<? super T>>) ((Collection<?>)methods);
   }

   public Set<Annotation> getDeclaredMetaAnnotations(Class<? extends Annotation> metaAnnotationType)
   {
      return Collections.unmodifiableSet(new ArraySet<Annotation>(declaredMetaAnnotationMap.get(metaAnnotationType)));
   }

   public boolean isDiscovered()
   {
      return discovered;
   }

}