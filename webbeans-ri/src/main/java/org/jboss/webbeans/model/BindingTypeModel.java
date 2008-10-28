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


package org.jboss.webbeans.model;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.DefinitionException;
import javax.webbeans.NonBinding;

import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.util.Reflections;

/**
 * 
 * Model of a binding type
 * 
 * @author Pete Muir
 *
 */
public class BindingTypeModel<T extends Annotation> extends AnnotationModel<T>
{
   
   private Set<AnnotatedMethod<?>> nonBindingTypes;
   private Integer hashCode;
   
   public BindingTypeModel(Class<T> type)
   {
      super(type);
   }
   
   @Override
   protected void init()
   {
      super.init();
      initNonBindingTypes();
      checkArrayAndAnnotationValuedMembers();
   }
   
   private void checkArrayAndAnnotationValuedMembers()
   {
      for (AnnotatedMethod<?> annotatedMethod : getAnnotatedAnnotation().getMembers())
      {
         if ((Reflections.isArrayType(annotatedMethod.getType()) || Annotation.class.isAssignableFrom(annotatedMethod.getType())) && !nonBindingTypes.contains(annotatedMethod))
         {
            throw new DefinitionException("Member of array type or annotation type must be annotated @NonBinding " + annotatedMethod);
         }
      }
      
   }

   @Override
   protected Class<? extends Annotation> getMetaAnnotation()
   {
      return BindingType.class;
   }
   
   public boolean hasNonBindingTypes()
   {
      return nonBindingTypes.size() > 0;
   }
   
   public Set<AnnotatedMethod<?>> getNonBindingTypes()
   {
      return nonBindingTypes;
   }
   
   protected void initNonBindingTypes()
   {
      nonBindingTypes = getAnnotatedAnnotation().getAnnotatedMembers(NonBinding.class);
   }
   
   public boolean isEqual(Annotation instance, Annotation other)
   {
      if (instance.annotationType().equals(getType()) && other.annotationType().equals(getType()))
      {
         for (AnnotatedMethod<?> annotatedMethod : getAnnotatedAnnotation().getMembers())
         {
            if (!nonBindingTypes.contains(annotatedMethod))
            {
               Object thisValue = Reflections.invokeAndWrap(annotatedMethod.getDelegate(), instance);
               Object thatValue = Reflections.invokeAndWrap(annotatedMethod.getDelegate(), other);
               if (!thisValue.equals(thatValue))
               {
                  return false;
               }
            }
         }
         return true;
      }
      return false;
   }
   
}
