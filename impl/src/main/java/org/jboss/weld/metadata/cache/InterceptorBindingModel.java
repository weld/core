/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.weld.metadata.cache;

import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.log.Log;
import org.jboss.weld.log.Logging;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.collections.Arrays2;

import javax.interceptor.InterceptorBinding;
import javax.enterprise.inject.Nonbinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * @author Marius Bogoevici
 */
public class InterceptorBindingModel<T extends Annotation> extends AnnotationModel<T>
{
   private static final Set<Class<? extends Annotation>> META_ANNOTATIONS = Arrays2.<Class<? extends Annotation>>asSet(InterceptorBinding.class);
   private static final Log log = Logging.getLog(BindingTypeModel.class);
   private Set<WeldMethod<?,?>> nonBindingTypes;
   private Set<Annotation> inheritedInterceptionBindingTypes;
   private Set<Annotation> metaAnnotations;

   public InterceptorBindingModel(Class<T> type, ClassTransformer transformer)
   {
      super(type, transformer);
      initNonBindingTypes();
      initInterceptionBindingTypes();
      this.metaAnnotations = getAnnotatedAnnotation().getAnnotations();
   }

   protected Set<Class<? extends Annotation>> getMetaAnnotationTypes()
   {
      return META_ANNOTATIONS;
   }

   public Set<Annotation> getMetaAnnotations()
   {
      return metaAnnotations;
   }

   protected void initNonBindingTypes()
   {
      nonBindingTypes = getAnnotatedAnnotation().getAnnotatedMembers(Nonbinding.class);
   }

   protected void initInterceptionBindingTypes()
   {
      inheritedInterceptionBindingTypes = getAnnotatedAnnotation().getMetaAnnotations(InterceptorBinding.class);
   }

   public Set<Annotation> getInheritedInterceptionBindingTypes()
   {
      return inheritedInterceptionBindingTypes;
   }

   public boolean isEqual(Annotation instance, Annotation other)
   {
       return isEqual(instance, other, false);
   }

   public boolean isEqual(Annotation instance, Annotation other, boolean includeNonBindingTypes)
   {
      if (instance.annotationType().equals(getRawType()) && other.annotationType().equals(getRawType()))
      {
         for (WeldMethod<?, ?> annotatedMethod : getAnnotatedAnnotation().getMembers())
         {
            if (includeNonBindingTypes || !nonBindingTypes.contains(annotatedMethod))
            {
               try
               {
                  Object thisValue = annotatedMethod.invoke(instance);
                  Object thatValue = annotatedMethod.invoke(other);
                  if (!thisValue.equals(thatValue))
                  {
                     return false;
                  }
               }
               catch (IllegalArgumentException e)
               {
                  throw new RuntimeException(e);
               }
               catch (IllegalAccessException e)
               {
                  throw new RuntimeException(e);
               }
               catch (InvocationTargetException e)
               {
                  throw new RuntimeException(e);
               }

            }
         }
         return true;
      }
      return false;
   }

}
