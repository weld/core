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

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Set;

import org.jboss.weld.introspector.AnnotationStore;
import org.jboss.weld.introspector.ForwardingWeldMember;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMember;
import org.jboss.weld.util.Reflections;

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
public abstract class AbstractWeldMember<T, X, S extends Member> extends AbstractWeldAnnotated<T, S> implements WeldMember<T, X, S>
{
   
   static abstract class WrappableForwardingAnnotatedMember<T, X, S extends Member> extends ForwardingWeldMember<T, X, S> implements WrappableAnnotatedItem<T, S>
   {
      
   }

   // The name of the member
   private final String name;

   // Cached string representation
   private String toString;
   private final boolean _public;
   private final boolean _private;
   private final boolean _packagePrivate;
   private final Package _package;
   private final WeldClass<X> declaringType;

   /**
    * Constructor
    * 
    * @param annotationMap The annotation map
    */
   protected AbstractWeldMember(AnnotationStore annotatedItemHelper, Member member, Class<T> rawType, Type type, Set<Type> typeClosure, WeldClass<X> declaringType)
   {
      super(annotatedItemHelper, rawType, type, typeClosure);
      name = member.getName();
      this._public = Modifier.isPublic(member.getModifiers());
      this._private = Modifier.isPrivate(member.getModifiers());
      this._packagePrivate = Reflections.isPackagePrivate(member.getModifiers());
      this._package = member.getDeclaringClass().getPackage();
      this.declaringType = declaringType;
   }

   /**
    * Indicates if the member is static
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
    * Indicates if the member if final
    * 
    * @return True if final, false otherwise
    * 
    * @see org.jboss.weld.introspector.WeldAnnotated#isFinal()
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

   /**
    * Gets the current value of the member
    * 
    * @param beanManager The Bean manager
    * @return The current value
    *
   public T getValue(BeanManager beanManager)
   {
      return beanManager.getInstance(getRawType(), getMetaAnnotationsAsArray(BindingType.class));
   }*/

   /**
    * Gets the name of the member
    * 
    * @returns The name
    * 
    * @see org.jboss.weld.introspector.WeldAnnotated#getName()
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

   public S getJavaMember()
   {
      return getDelegate();
   }
   
   public WeldClass<X> getDeclaringType()
   {
      return declaringType;
   }

}
