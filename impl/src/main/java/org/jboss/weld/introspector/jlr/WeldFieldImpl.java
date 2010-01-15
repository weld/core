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

import static org.jboss.weld.logging.messages.UtilMessage.ACCESS_ERROR_ON_FIELD;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedField;

import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.jboss.weld.util.reflection.SecureReflections;

/**
 * Represents an annotated field
 * 
 * This class is immutable, and therefore threadsafe
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class WeldFieldImpl<T, X> extends AbstractWeldMember<T, X, Field> implements WeldField<T, X>
{

   // The underlying field
   private final Field field;

   public static <T, X> WeldFieldImpl<T, X> of(Field field, WeldClass<X> declaringClass, ClassTransformer classTransformer)
   {
      return new WeldFieldImpl<T, X>(field, (Class<T>) field.getType(), field.getGenericType(), new HierarchyDiscovery(field.getGenericType()).getTypeClosure(), buildAnnotationMap(field.getAnnotations()), buildAnnotationMap(field.getDeclaredAnnotations()), declaringClass, classTransformer);
   }

   public static <T, X> WeldFieldImpl<T, X> of(AnnotatedField<? super X> annotatedField, WeldClass<X> declaringClass, ClassTransformer classTransformer)
   {
      return new WeldFieldImpl<T, X>(annotatedField.getJavaMember(), (Class<T>) annotatedField.getJavaMember().getType(), annotatedField.getBaseType(), annotatedField.getTypeClosure(), buildAnnotationMap(annotatedField.getAnnotations()), buildAnnotationMap(annotatedField.getAnnotations()), declaringClass, classTransformer);
   }

   /**
    * Constructor
    * 
    * Initializes the superclass with the built annotation map and detects the
    * type arguments
    * 
    * @param field The actual field
    * @param declaringClass The abstraction of the declaring class
    */
   private WeldFieldImpl(Field field, final Class<T> rawType, final Type type, Set<Type> typeClosure, Map<Class<? extends Annotation>, Annotation> annotationMap, Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap, WeldClass<X> declaringClass, ClassTransformer classTransformer)
   {
      super(annotationMap, declaredAnnotationMap, classTransformer, field, rawType, type, typeClosure, declaringClass);
      this.field = field;
   }

   /**
    * Gets the underlying field
    * 
    * @return The fields
    */
   public Field getAnnotatedField()
   {
      return field;
   }

   @Override
   public Field getDelegate()
   {
      return field;
   }

   public void set(Object instance, Object value) throws IllegalArgumentException, IllegalAccessException
   {
      SecureReflections.ensureFieldAccessible(field).set(instance, value);
   }

   public void setOnInstance(Object instance, Object value) throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException
   {
      SecureReflections.getField(instance.getClass(), getName()).set(instance, value);
   }

   @SuppressWarnings("unchecked")
   public T get(Object instance)
   {
      try
      {
         return (T) SecureReflections.ensureFieldAccessible(getDelegate()).get(instance);
      }
      catch (Exception e)
      {
         throw new WeldException(ACCESS_ERROR_ON_FIELD, e, getDelegate().getName(), getDelegate().getDeclaringClass());
      }
   }

   /**
    * Gets the property name
    * 
    * @return The property name
    * 
    * @see org.jboss.weld.introspector.WeldField#getName()
    */
   public String getPropertyName()
   {
      return getName();
   }

   /**
    * Gets a string representation of the field
    * 
    * @return A string representation
    */
   @Override
   public String toString()
   {
      return new StringBuilder().append("field ").append(getDeclaringType().getName()).append(".").append(field.getName()).toString();
   }

   @Override
   public boolean equals(Object other)
   {
      if (other instanceof WeldField<?, ?>)
      {
         WeldField<?, ?> that = (WeldField<?, ?>) other;
         return this.getDeclaringType().equals(that.getDeclaringType()) && this.getName().equals(that.getName());
      }
      else
      {
         return false;
      }
   }

   @Override
   public int hashCode()
   {
      return getDelegate().hashCode();
   }

}
