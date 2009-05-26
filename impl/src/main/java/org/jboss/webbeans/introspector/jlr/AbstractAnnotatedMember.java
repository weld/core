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

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import javax.enterprise.inject.BindingType;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.webbeans.introspector.AnnotatedMember;
import org.jboss.webbeans.introspector.AnnotationStore;
import org.jboss.webbeans.introspector.ForwardingAnnotatedMember;
import org.jboss.webbeans.util.Reflections;

/**
 * Represents an abstract annotated memeber (field, method or constructor)
 * 
 * This class is immutable, and therefore threadsafe
 * 
 * @author Pete Muir
 * 
 * @param <T>
 * @param <S>
 */
public abstract class AbstractAnnotatedMember<T, S extends Member> extends AbstractAnnotatedItem<T, S> implements AnnotatedMember<T, S>
{
   
   static abstract class WrappableForwardingAnnotatedMember<T, S extends Member> extends ForwardingAnnotatedMember<T, S> implements WrappableAnnotatedItem<T, S>
   {
      
   }

   // The name of the member
   private final String name;

   // Cached string representation
   private String toString;
   
   private final boolean _public;

   /**
    * Constructor
    * 
    * @param annotationMap The annotation map
    */
   protected AbstractAnnotatedMember(AnnotationStore annotatedItemHelper, Member member, Class<T> rawType, Type type)
   {
      super(annotatedItemHelper, rawType, type);
      name = member.getName();
      _public = Modifier.isPublic(member.getModifiers());
   }

   /**
    * Indicates if the member is static
    * 
    * @return True if static, false otherwise
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#isStatic()
    */
   public boolean isStatic()
   {
      return Reflections.isStatic(getDelegate());
   }

   /**
    * Indicates if the member if final
    * 
    * @return True if final, false otherwise
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#isFinal()
    */
   public boolean isFinal()
   {
      return Reflections.isFinal(getDelegate());
   }

   public boolean isTransient()
   {
      return Reflections.isTransient(getDelegate());
   }
   
   public boolean isPublic()
   {
      return _public;
   }

   /**
    * Gets the current value of the member
    * 
    * @param beanManager The Web Beans manager
    * @return The current value
    */
   public T getValue(BeanManager beanManager)
   {
      return beanManager.getInstanceByType(getRawType(), getMetaAnnotationsAsArray(BindingType.class));
   }

   /**
    * Gets the name of the member
    * 
    * @returns The name
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#getName()
    */
   public String getName()
   {
      return name;
   }

   /**
    * Gets a string representation of the member
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
      toString = "Abstract annotated member " + getName();
      return toString;
   }

   public S getMember()
   {
      return getDelegate();
   }

}
