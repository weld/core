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
package org.jboss.weld.introspector.jlr;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
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
import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.introspector.AnnotationStore;
import org.jboss.weld.introspector.ConstructorSignature;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.WBClass;
import org.jboss.weld.introspector.WBConstructor;
import org.jboss.weld.introspector.WBField;
import org.jboss.weld.introspector.WBMethod;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.Names;
import org.jboss.weld.util.Reflections;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

/**
 * Represents an annotated class
 * 
 * This class is immutable, and therefore threadsafe
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class WBClassImpl<T> extends AbstractWBAnnotated<T, Class<T>> implements WBClass<T>
{
   
// The superclass abstraction of the type
   private final WBClass<?> superclass;
   // The name of the type
   private final String name;
   
   private final String _simpleName;
   private final boolean _public;
   private final boolean _private;
   private final boolean _packagePrivate;
   private final Package _package;
  
   private static List<Class<?>> NO_ARGUMENTS = Collections.emptyList();

   // The set of abstracted fields
   private final Set<WBField<?, ?>> fields;
   // The map from annotation type to abstracted field with annotation
   private final SetMultimap<Class<? extends Annotation>, WBField<?, ?>> annotatedFields;
   // The map from annotation type to abstracted field with meta-annotation
   private final SetMultimap<Class<? extends Annotation>, WBField<?, ?>> metaAnnotatedFields;

   // The set of abstracted fields
   private final Set<WBField<?, ?>> declaredFields;
   private final Map<String, WBField<?, ?>> declaredFieldsByName;
   // The map from annotation type to abstracted field with annotation
   private final SetMultimap<Class<? extends Annotation>, WBField<?, T>> declaredAnnotatedFields;
   // The map from annotation type to abstracted field with meta-annotation
   private final SetMultimap<Class<? extends Annotation>, WBField<?, ?>> declaredMetaAnnotatedFields;

   // The set of abstracted methods
   private final Set<WBMethod<?, ?>> methods;
   private final Map<MethodSignature, WBMethod<?, ?>> declaredMethodsBySignature;
   private final Map<MethodSignature, WBMethod<?, ?>> methodsBySignature;
   // The map from annotation type to abstracted method with annotation
   private final SetMultimap<Class<? extends Annotation>, WBMethod<?, ?>> annotatedMethods;
   // The map from annotation type to method with a parameter with annotation
   private final SetMultimap<Class<? extends Annotation>, WBMethod<?, ?>> methodsByAnnotatedParameters;

   // The set of abstracted methods
   private final Set<WBMethod<?, ?>> declaredMethods;
   // The map from annotation type to abstracted method with annotation
   private final SetMultimap<Class<? extends Annotation>, WBMethod<?, T>> declaredAnnotatedMethods;
   // The map from annotation type to method with a parameter with annotation
   private final SetMultimap<Class<? extends Annotation>, WBMethod<?, T>> declaredMethodsByAnnotatedParameters;

   // The set of abstracted constructors
   private final Set<WBConstructor<T>> constructors;
   private final Map<ConstructorSignature, WBConstructor<?>> declaredConstructorsBySignature;
   // The map from annotation type to abstracted constructor with annotation
   private final SetMultimap<Class<? extends Annotation>, WBConstructor<T>> annotatedConstructors;
   // The map from class list to abstracted constructor
   private final Map<List<Class<?>>, WBConstructor<T>> constructorsByArgumentMap;

   private final SetMultimap<Class<? extends Annotation>, WBConstructor<?>> constructorsByAnnotatedParameters;

   // Cached string representation
   private final String toString;

   private final boolean _nonStaticMemberClass;
   private final boolean _abstract;
   private final boolean _enum;

   public static <T> WBClass<T> of(Class<T> clazz, ClassTransformer classTransformer)
   {
      AnnotationStore annotationStore = AnnotationStore.of(clazz.getAnnotations(), clazz.getDeclaredAnnotations(), classTransformer.getTypeStore());
      return new WBClassImpl<T>(clazz, clazz, null, annotationStore, classTransformer);
   }

   public static <T> WBClass<T> of(AnnotatedType<T> annotatedType, ClassTransformer classTransformer)
   {
      AnnotationStore annotationStore = AnnotationStore.of(annotatedType.getAnnotations(), annotatedType.getAnnotations(), classTransformer.getTypeStore());
      return new WBClassImpl<T>(annotatedType.getJavaClass(), annotatedType.getBaseType(), annotatedType, annotationStore, classTransformer);
   }

   protected WBClassImpl(Class<T> rawType, Type type, AnnotatedType<T> annotatedType, AnnotationStore annotationStore, ClassTransformer classTransformer)
   {
      super(annotationStore, rawType, type);
      this.toString = "class " + Names.classToString(rawType);
      this.name = rawType.getName();
      this._simpleName = rawType.getSimpleName();
      if (rawType.getSuperclass() != null)
      {
         this.superclass = classTransformer.loadClass(rawType.getSuperclass());
      }
      else
      {
         this.superclass = null;
      }
      this._public = Modifier.isFinal(rawType.getModifiers());
      this._private = Modifier.isPrivate(rawType.getModifiers());
      this._packagePrivate = Reflections.isPackagePrivate(rawType.getModifiers());
      this._package = rawType.getPackage();
      this.fields = new HashSet<WBField<?, ?>>();
      this.annotatedFields = Multimaps.newSetMultimap(new HashMap<Class<? extends Annotation>, Collection<WBField<?, ?>>>(), new Supplier< Set<WBField<?, ?>>>()
      {
         
         public Set<WBField<?, ?>> get()
         {
            return new HashSet<WBField<?, ?>>();
         }
        
      });
      this.metaAnnotatedFields = Multimaps.newSetMultimap(new HashMap<Class<? extends Annotation>, Collection<WBField<?, ?>>>(), new Supplier< Set<WBField<?, ?>>>()
      {
         
         public Set<WBField<?, ?>> get()
         {
            return new HashSet<WBField<?, ?>>();
         }
        
      });
      this.declaredFields = new HashSet<WBField<?, ?>>();
      this.declaredFieldsByName = new HashMap<String, WBField<?, ?>>();
      this.declaredAnnotatedFields = Multimaps.newSetMultimap(new HashMap<Class<? extends Annotation>, Collection<WBField<?, T>>>(), new Supplier< Set<WBField<?, T>>>()
      {
         
         public Set<WBField<?, T>> get()
         {
            return new HashSet<WBField<?, T>>();
         }
        
      });
      this.declaredMetaAnnotatedFields = Multimaps.newSetMultimap(new HashMap<Class<? extends Annotation>, Collection<WBField<?, ?>>>(), new Supplier< Set<WBField<?, ?>>>()
      {
         
         public Set<WBField<?, ?>> get()
         {
            return new HashSet<WBField<?, ?>>();
         }
        
      });
      this._nonStaticMemberClass = Reflections.isNonStaticInnerClass(rawType);
      this._abstract = Reflections.isAbstract(rawType);
      this._enum = rawType.isEnum();

      
      Map<Field, AnnotatedField<? super T>> annotatedTypeFields = new HashMap<Field, AnnotatedField<? super T>>();
      if (annotatedType != null)
      {
         for (AnnotatedField<? super T> annotatedField : annotatedType.getFields())
         {
            annotatedTypeFields.put(annotatedField.getJavaMember(), annotatedField);
         }
      }
      
      for (Class<?> c = rawType; c != Object.class && c != null; c = c.getSuperclass())
      {
         for (Field field : c.getDeclaredFields())
         {
            if (!field.isAccessible())
            {
               field.setAccessible(true);
            }
            WBField<?, T> annotatedField = null;
            if (annotatedTypeFields.containsKey(field))
            {
               annotatedField = WBFieldImpl.of(annotatedTypeFields.get(field), this.<T>getDeclaringWBClass(field, classTransformer), classTransformer);
            }
            else
            {
               annotatedField = WBFieldImpl.of(field, this.<T>getDeclaringWBClass(field, classTransformer), classTransformer);
            }
            
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
      this.annotatedConstructors = Multimaps.newSetMultimap(new HashMap<Class<? extends Annotation>, Collection<WBConstructor<T>>>(), new Supplier< Set<WBConstructor<T>>>()
      {
         
         public Set<WBConstructor<T>> get()
         {
            return new HashSet<WBConstructor<T>>();
         }
        
      });
      this.constructorsByAnnotatedParameters = Multimaps.newSetMultimap(new HashMap<Class<? extends Annotation>, Collection<WBConstructor<?>>>(), new Supplier< Set<WBConstructor<?>>>()
      {
         
         public Set<WBConstructor<?>> get()
         {
            return new HashSet<WBConstructor<?>>();
         }
        
      });
      
      Map<Constructor<? super T>, AnnotatedConstructor<T>> annotatedTypeConstructors = new HashMap<Constructor<? super T>, AnnotatedConstructor<T>>();
      if (annotatedType != null)
      {
         for (AnnotatedConstructor<T> annotated : annotatedType.getConstructors())
         {
            annotatedTypeConstructors.put(annotated.getJavaMember(), annotated);
         }
      }
      
      this.declaredConstructorsBySignature = new HashMap<ConstructorSignature, WBConstructor<?>>();
      for (Constructor<?> constructor : rawType.getDeclaredConstructors())
      {
         WBConstructor<T> annotatedConstructor = null;
         if (annotatedTypeConstructors.containsKey(constructor))
         {
            WBClass<T> declaringClass = this.getDeclaringWBClass(constructor, classTransformer);
            annotatedConstructor = WBConstructorImpl.of(annotatedTypeConstructors.get(constructor), declaringClass, classTransformer);
         }
         else
         {
            // TODO Fix this cast
            Constructor<T> c = (Constructor<T>) constructor;
            annotatedConstructor = WBConstructorImpl.of(c, this.<T>getDeclaringWBClass(c, classTransformer), classTransformer);
         }
         
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
               annotatedConstructors.putAll(annotation.annotationType(), new HashSet<WBConstructor<T>>());
            }
            annotatedConstructors.get(annotation.annotationType()).add(annotatedConstructor);
         }

         for (Class<? extends Annotation> annotationType : WBConstructor.MAPPED_PARAMETER_ANNOTATIONS)
         {
            if (annotatedConstructor.getAnnotatedWBParameters(annotationType).size() > 0)
            {
               constructorsByAnnotatedParameters.put(annotationType, annotatedConstructor);
            }
         }
      }

      this.methods = new HashSet<WBMethod<?, ?>>();
      this.annotatedMethods = Multimaps.newSetMultimap(new HashMap<Class<? extends Annotation>, Collection<WBMethod<?, ?>>>(), new Supplier< Set<WBMethod<?, ?>>>()
      {
         
         public Set<WBMethod<?, ?>> get()
         {
            return new HashSet<WBMethod<?, ?>>();
         }
        
      });
      this.methodsByAnnotatedParameters = Multimaps.newSetMultimap(new HashMap<Class<? extends Annotation>, Collection<WBMethod<?, ?>>>(), new Supplier< Set<WBMethod<?, ?>>>()
      {
         
         public Set<WBMethod<?, ?>> get()
         {
            return new HashSet<WBMethod<?, ?>>();
         }
        
      });
      this.declaredMethods = new HashSet<WBMethod<?, ?>>();
      this.declaredAnnotatedMethods = Multimaps.newSetMultimap(new HashMap<Class<? extends Annotation>, Collection<WBMethod<?, T>>>(), new Supplier< Set<WBMethod<?, T>>>()
      {
         
         public Set<WBMethod<?, T>> get()
         {
            return new HashSet<WBMethod<?, T>>();
         }
        
      });
      this.declaredMethodsByAnnotatedParameters = Multimaps.newSetMultimap(new HashMap<Class<? extends Annotation>, Collection<WBMethod<?, T>>>(), new Supplier< Set<WBMethod<?, T>>>()
      {
         
         public Set<WBMethod<?, T>> get()
         {
            return new HashSet<WBMethod<?, T>>();
         }
        
      });
      this.declaredMethodsBySignature = new HashMap<MethodSignature, WBMethod<?, ?>>();
      this.methodsBySignature = new HashMap<MethodSignature, WBMethod<?, ?>>();
      
      Map<Method, AnnotatedMethod<?>> annotatedTypeMethods = new HashMap<Method, AnnotatedMethod<?>>();
      if (annotatedType != null)
      {
         for (AnnotatedMethod<?> annotated : annotatedType.getMethods())
         {
            annotatedTypeMethods.put(annotated.getJavaMember(), annotated);
         }
      }
      
      for (Class<?> c = rawType; c != Object.class && c != null; c = c.getSuperclass())
      {
         for (Method method : c.getDeclaredMethods())
         {
            if (!method.isAccessible())
            {
               method.setAccessible(true);
            }

            WBMethod<?, T> annotatedMethod = null;
            if (annotatedTypeMethods.containsKey(method))
            {
               annotatedMethod = WBMethodImpl.of(annotatedTypeMethods.get(method), this, classTransformer);
            }
            else
            {
               annotatedMethod = WBMethodImpl.of(method, this.<T>getDeclaringWBClass(method, classTransformer), classTransformer);
            }
            this.methods.add(annotatedMethod);
            this.methodsBySignature.put(annotatedMethod.getSignature(), annotatedMethod);
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
               if (annotatedMethod.getAnnotatedWBParameters(annotationType).size() > 0)
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
   
   @SuppressWarnings("unchecked")
   private <X> WBClass<X> getDeclaringWBClass(Member member, ClassTransformer transformer)
   {
      if (member.getDeclaringClass().equals(getJavaClass()))
      {
         return (WBClass<X>) this;
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
   public Set<WBField<?, ?>> getWBFields()
   {
      return Collections.unmodifiableSet(fields);
   }

   public Set<WBField<?, ?>> getDeclaredFields()
   {
      return Collections.unmodifiableSet(declaredFields);
   }

   public <F> WBField<F, ?> getDeclaredWBField(String fieldName, WBClass<F> expectedType)
   {
      return (WBField<F, ?>) declaredFieldsByName.get(fieldName);
   }

   public Set<WBField<?, T>> getDeclaredAnnotatedWBFields(Class<? extends Annotation> annotationType)
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
   public Set<WBConstructor<T>> getWBConstructors()
   {
      return Collections.unmodifiableSet(constructors);
   }

   public WBConstructor<T> getDeclaredWBConstructor(ConstructorSignature signature)
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
    * @param metaAnnotationType The meta-annotation type to match
    * @return The set of abstracted fields with meta-annotation present. Returns
    *         an empty set if no matches are found.
    */
   public Set<WBField<?, ?>> getMetaAnnotatedWBFields(Class<? extends Annotation> metaAnnotationType)
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
   public Set<WBField<?, ?>> getAnnotatedWBFields(Class<? extends Annotation> annotationType)
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
    * @param annotationType The annotation type to match
    * @return A set of matching method abstractions. Returns an empty set if no
    *         matches are found.
    * 
    * @see org.jboss.weld.introspector.WBClass#getAnnotatedWBMethods(Class)
    */
   public Set<WBMethod<?, ?>> getAnnotatedWBMethods(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableSet(annotatedMethods.get(annotationType));
   }

   public Set<WBMethod<?, T>> getDeclaredAnnotatedWBMethods(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableSet(declaredAnnotatedMethods.get(annotationType));
   }

   /**
    * Gets constructors with given annotation type
    * 
    * @param annotationType The annotation type to match
    * @return A set of abstracted constructors with given annotation type. If
    *         the constructors set is empty, initialize it first. Returns an
    *         empty set if there are no matches.
    * 
    * @see org.jboss.weld.introspector.WBClass#getAnnotatedWBConstructors(Class)
    */
   public Set<WBConstructor<T>> getAnnotatedWBConstructors(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableSet(annotatedConstructors.get(annotationType));
   }

   public WBConstructor<T> getNoArgsWBConstructor()
   {
      return constructorsByArgumentMap.get(NO_ARGUMENTS);
   }

   public Set<WBMethod<?, ?>> getWBMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableSet(methodsByAnnotatedParameters.get(annotationType));
   }

   public Set<WBConstructor<?>> getWBConstructorsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableSet(constructorsByAnnotatedParameters.get(annotationType));
   }

   public Set<WBMethod<?, T>> getDeclaredWBMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableSet(declaredMethodsByAnnotatedParameters.get(annotationType));
   }

   public WBMethod<?, ?> getWBMethod(Method methodDescriptor)
   {
      // TODO Should be cached
      for (WBMethod<?, ?> annotatedMethod : methods)
      {
         if (annotatedMethod.getName().equals(methodDescriptor.getName()) && Arrays.equals(annotatedMethod.getParameterTypesAsArray(), methodDescriptor.getParameterTypes()))
         {
            return annotatedMethod;
         }
      }
      return null;
   }

   public Set<WBMethod<?, ?>> getWBMethods()
   {
      return Collections.unmodifiableSet(methods);
   }

   public WBMethod<?, ?> getDeclaredWBMethod(Method method)
   {
      // TODO Should be cached
      for (WBMethod<?, ?> annotatedMethod : declaredMethods)
      {
         if (annotatedMethod.getName().equals(method.getName()) && Arrays.equals(annotatedMethod.getParameterTypesAsArray(), method.getParameterTypes()))
         {
            return annotatedMethod;
         }
      }
      return null;
   }
   
   public Set<WBMethod<?, ?>> getDeclaredWBMethods()
   {
      return declaredMethods;
   }

   @SuppressWarnings("unchecked")
   public <M> WBMethod<M, ?> getDeclaredWBMethod(MethodSignature signature, WBClass<M> expectedReturnType)
   {
      return (WBMethod<M, ?>) declaredMethodsBySignature.get(signature);
   }

   @SuppressWarnings("unchecked")
   public <M> WBMethod<M, ?> getWBMethod(MethodSignature signature)
   {
      return (WBMethod<M, ?>) methodsBySignature.get(signature);
   }

   /**
    * Gets a string representation of the class
    * 
    * @return A string representation
    */
   @Override
   public String toString()
   {
      return toString;
   }
   
   public String getSimpleName()
   {
      return _simpleName;
   }
   
   /**
    * Indicates if the type is static
    * 
    * @return True if static, false otherwise
    * 
    * @see org.jboss.weld.introspector.WBAnnotated#isStatic()
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
    * @see org.jboss.weld.introspector.WBAnnotated#isFinal()
    */
   public boolean isFinal()
   {
      return Reflections.isFinal(getDelegate());
   }
   
   public boolean isPublic()
   {
      return _public;
   }

   /**
    * Gets the name of the type
    * 
    * @returns The name
    * 
    * @see org.jboss.weld.introspector.WBAnnotated#getName()
    */
   public String getName()
   {
      return name;
   }

   /**
    * Gets the superclass abstraction of the type
    * 
    * @return The superclass abstraction
    */
   public WBClass<?> getWBSuperclass()
   {
      return superclass;
   }
   
   public boolean isEquivalent(Class<?> clazz)
   {
      return getDelegate().equals(clazz);
   }
   
   public boolean isPrivate()
   {
      return _private;
   }
   
   public boolean isPackagePrivate()
   {
      return _packagePrivate;
   }
   
   public Package getPackage()
   {
      return _package;
   }

   @SuppressWarnings("unchecked")
   public <U> WBClass<? extends U> asWBSubclass(WBClass<U> clazz)
   {
      return (WBClass<? extends U>) this;
   }

   @SuppressWarnings("unchecked")
   public <S> S cast(Object object)
   {
      return (S) object;
   }

   @SuppressWarnings("unchecked")
   public Set<AnnotatedConstructor<T>> getConstructors()
   {
      return (Set) constructors;
   }

   @SuppressWarnings("unchecked")
   public Set<AnnotatedField<? super T>> getFields()
   {
      return (Set) fields;
   }

   @SuppressWarnings("unchecked")
   public Set<AnnotatedMethod<? super T>> getMethods()
   {
      return (Set) methods;
   }

}