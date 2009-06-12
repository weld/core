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

import java.lang.reflect.Field;

import org.jboss.webbeans.introspector.WBField;
import org.jboss.webbeans.introspector.WBType;
import org.jboss.webbeans.introspector.AnnotationStore;
import org.jboss.webbeans.util.Names;
import org.jboss.webbeans.util.Reflections;

/**
 * Represents an annotated field
 * 
 * This class is immutable, and therefore threadsafe
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class WBFieldImpl<T> extends AbstractWBMember<T, Field> implements WBField<T>
{
   
   // The underlying field
   private final Field field;
   // The abstraction of the declaring class
   private final WBType<?> declaringClass;

   // Cached string representation
   private String toString;

   /**
    * Constructor
    * 
    * Initializes the superclass with the built annotation map and detects the
    * type arguments
    * 
    * @param field The actual field
    * @param declaringClass The abstraction of the declaring class
    */
   protected WBFieldImpl(Field field, WBType<?> declaringClass)
   {
      super(AnnotationStore.of(field), field, (Class<T>) field.getType(), field.getGenericType());
      this.field = field;
      field.setAccessible(true);
      this.declaringClass = declaringClass;
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

   public Field getDelegate()
   {
      return field;
   }
   
   public void set(Object instance, Object value) throws IllegalArgumentException, IllegalAccessException
   {
      field.set(instance, value);
   }
   
   public void setOnInstance(Object instance, Object value) throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException
   {
      instance.getClass().getField(getName()).set(instance, value);
   }

   @SuppressWarnings("unchecked")
   public T get(Object instance)
   {
      return (T) Reflections.getAndWrap(getDelegate(), instance);
   }

   /**
    * Gets the property name
    * 
    * @return The property name
    * 
    * @see org.jboss.webbeans.introspector.WBField#getName()
    */
   public String getPropertyName()
   {
      return getName();
   }

   /**
    * Gets the abstracted declaring class
    * 
    * @return The declaring class
    * 
    * @see org.jboss.webbeans.introspector.WBField#getDeclaringClass()
    */
   public WBType<?> getDeclaringClass()
   {
      return declaringClass;
   }

   /**
    * Gets a string representation of the field
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
      toString = Names.fieldToString(field) + " on " + getDeclaringClass();
      return toString;
   }

   @Override
   public boolean equals(Object other)
   {
      if (other instanceof WBField)
      {
         WBField<?> that = (WBField<?>) other;
         return this.getDeclaringClass().equals(that.getDeclaringClass()) && this.getName().equals(that.getName());
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
