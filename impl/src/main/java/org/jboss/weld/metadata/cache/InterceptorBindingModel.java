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

import static org.jboss.weld.logging.Category.REFLECTION;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.MetadataMessage.NON_BINDING_MEMBER_TYPE;
import static org.jboss.weld.logging.messages.ReflectionMessage.MISSING_TARGET;
import static org.jboss.weld.logging.messages.ReflectionMessage.MISSING_TARGET_TYPE_METHOD_OR_TARGET_TYPE;
import static org.jboss.weld.logging.messages.ReflectionMessage.TARGET_TYPE_METHOD_INHERITS_FROM_TARGET_TYPE;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.collections.Arrays2;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;

/**
 * @author Marius Bogoevici
 */
public class InterceptorBindingModel<T extends Annotation> extends AnnotationModel<T>
{
   private static final Set<Class<? extends Annotation>> META_ANNOTATIONS = Arrays2.<Class<? extends Annotation>>asSet(InterceptorBinding.class);
   private static final LocLogger log = loggerFactory().getLogger(REFLECTION);
   private Set<WeldMethod<?,?>> nonBindingTypes;
   private Set<Annotation> inheritedInterceptionBindingTypes;
   private Set<Annotation> metaAnnotations;

   public InterceptorBindingModel(Class<T> type, ClassTransformer transformer)
   {
      super(type, transformer);
      if (isValid())
      {
         initNonBindingTypes();
         initInterceptionBindingTypes();
         checkArrayAndAnnotationValuedMembers();
         checkMetaAnnotations();
         this.metaAnnotations = getAnnotatedAnnotation().getAnnotations();
      }
   }

   @Override
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
      nonBindingTypes = getAnnotatedAnnotation().getMembers(Nonbinding.class);
   }

   protected void initInterceptionBindingTypes()
   {
      inheritedInterceptionBindingTypes = getAnnotatedAnnotation().getMetaAnnotations(InterceptorBinding.class);
   }

   @Override
   protected void initValid()
   {
      super.initValid();
      if (!getAnnotatedAnnotation().isAnnotationPresent(Target.class))
      {
         this.valid = false;
         log.debug(MISSING_TARGET, getAnnotatedAnnotation());
      }
      else
      {
         ElementType[] targetElementTypes = getAnnotatedAnnotation().getAnnotation(Target.class).value();
         if (!Arrays2.unorderedEquals(targetElementTypes, ElementType.TYPE, ElementType.METHOD)
               && !Arrays2.unorderedEquals(targetElementTypes, ElementType.TYPE))
         {
            this.valid = false;
            log.debug(MISSING_TARGET_TYPE_METHOD_OR_TARGET_TYPE, getAnnotatedAnnotation());
         }
      }
   }

   private void checkMetaAnnotations()
   {
      if (Arrays2.containsAll(getAnnotatedAnnotation().getAnnotation(Target.class).value(), ElementType.METHOD))
      {
         for (Annotation inheritedBinding: getInheritedInterceptionBindingTypes())
         {
            if (!Arrays2.containsAll(inheritedBinding.annotationType().getAnnotation(Target.class).value(), ElementType.METHOD))
            {
               this.valid = false;
               log.debug(TARGET_TYPE_METHOD_INHERITS_FROM_TARGET_TYPE, 
                     getAnnotatedAnnotation(), inheritedBinding);
            }
         }
      }
   }

   private void checkArrayAndAnnotationValuedMembers()
   {
      for (WeldMethod<?, ?> annotatedMethod : getAnnotatedAnnotation().getMembers())
      {
         if ((Reflections.isArrayType(annotatedMethod.getJavaClass()) || Annotation.class.isAssignableFrom(annotatedMethod.getJavaClass())) && !nonBindingTypes.contains(annotatedMethod))
         {
            throw new DefinitionException(NON_BINDING_MEMBER_TYPE, annotatedMethod);
         }
      }
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
                  throw new WeldException(e);
               }
               catch (IllegalAccessException e)
               {
                  throw new WeldException(e);
               }
               catch (InvocationTargetException e)
               {
                  throw new WeldException(e);
               }

            }
         }
         return true;
      }
      return false;
   }

}
