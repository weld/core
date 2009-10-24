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
package org.jboss.weld.metadata.cache;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Annotation;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.NormalScope;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Stereotype;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Scope;
import javax.interceptor.InterceptorBinding;

import org.jboss.weld.DefinitionException;
import org.jboss.weld.log.Log;
import org.jboss.weld.log.Logging;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.collections.Arrays2;

/**
 * A meta model for a stereotype, allows us to cache a stereotype and to
 * validate it
 * 
 * @author Pete Muir
 * 
 */
public class StereotypeModel<T extends Annotation> extends AnnotationModel<T>
{
   private static final Set<Class<? extends Annotation>> META_ANNOTATIONS = Arrays2.<Class<? extends Annotation>>asSet(Stereotype.class);
   private static final Log log = Logging.getLog(StereotypeModel.class);
   
   // Is the stereotype a policy
   private boolean policy;
   // The default scope type
   private Annotation defaultScopeType;
   // Is the bean name defaulted
   private boolean beanNameDefaulted;
   // The interceptor bindings
   private Set<Annotation> interceptorBindings;
   
   private Set<Annotation> inheritedSterotypes;
   
   private Set<Annotation> metaAnnotations;

   /**
    * Constructor
    * 
    * @param sterotype The stereotype
    */
   public StereotypeModel(Class<T> sterotype, ClassTransformer transformer)
   {
      super(sterotype, transformer);
      initPolicy();
      initDefaultScopeType();
      initBeanNameDefaulted();
      initInterceptorBindings();
      initInheritedStereotypes();
      checkBindings();
      this.metaAnnotations = getAnnotatedAnnotation().getAnnotations();
   }

   /**
    * Validates the binding types
    */
   private void checkBindings()
   {
      Set<Annotation> bindings = getAnnotatedAnnotation().getMetaAnnotations(Qualifier.class);
      if (bindings.size() > 0)
      {
         for (Annotation annotation : bindings)
         {
            if (!annotation.annotationType().equals(Named.class))
            {
               throw new DefinitionException("Cannot declare binding types on a stereotype " + getAnnotatedAnnotation());
            }
         }
      }
   }

   /**
    * Initializes the interceptor bindings
    */
   private void initInterceptorBindings()
   {
      interceptorBindings = getAnnotatedAnnotation().getMetaAnnotations(InterceptorBinding.class);
   }
   
   private void initInheritedStereotypes()
   {
      this.inheritedSterotypes = getAnnotatedAnnotation().getMetaAnnotations(Stereotype.class);
   }

   /**
    * Initializes the bean name defaulted
    */
   private void initBeanNameDefaulted()
   {
      if (getAnnotatedAnnotation().isAnnotationPresent(Named.class))
      {
         if (!"".equals(getAnnotatedAnnotation().getAnnotation(Named.class).value()))
         {
            throw new DefinitionException("Cannot specify a value for a @Named stereotype " + getAnnotatedAnnotation());
         }
         beanNameDefaulted = true;
      }
   }

   /**
    * Initializes the default scope type
    */
   private void initDefaultScopeType()
   {
      Set<Annotation> scopeTypes = new HashSet<Annotation>();
      scopeTypes.addAll(getAnnotatedAnnotation().getMetaAnnotations(Scope.class));
      scopeTypes.addAll(getAnnotatedAnnotation().getMetaAnnotations(NormalScope.class));
      if (scopeTypes.size() > 1)
      {
         throw new DefinitionException("At most one scope type may be specified for " + getAnnotatedAnnotation());
      }
      else if (scopeTypes.size() == 1)
      {
         this.defaultScopeType = scopeTypes.iterator().next();
      }
   }

   /**
    * Initializes the default deployment type
    */
   private void initPolicy()
   {
      if (getAnnotatedAnnotation().isAnnotationPresent(Alternative.class))
      {
         this.policy = true;
      }
   }
   
   @Override
   protected void initValid()
   {
      super.initValid();
      if (!getAnnotatedAnnotation().isAnnotationPresent(Target.class))
      {
         this.valid = false;
         log.debug("#0 is missing @Target annotation.", getAnnotatedAnnotation());
      }
      else if (!(
            Arrays2.unorderedEquals(getAnnotatedAnnotation().getAnnotation(Target.class).value(), METHOD, FIELD, TYPE) ||
            Arrays2.unorderedEquals(getAnnotatedAnnotation().getAnnotation(Target.class).value(), TYPE) ||
            Arrays2.unorderedEquals(getAnnotatedAnnotation().getAnnotation(Target.class).value(), METHOD) ||
            Arrays2.unorderedEquals(getAnnotatedAnnotation().getAnnotation(Target.class).value(), FIELD) ||
            Arrays2.unorderedEquals(getAnnotatedAnnotation().getAnnotation(Target.class).value(), METHOD, TYPE)
         ))
      {
         this.valid = false;
         log.debug("#0 is has incorrect @Target annotation. Should be @Target(METHOD, FIELD, TYPE, PARAMETER), @Target(METHOD, TYPE), @Target(METHOD), @Target(TYPE) or @Target(FIELD).", getAnnotatedAnnotation());
      }
   }

   

   /**
    * Get the default scope type the stereotype specifies
    * 
    * @return The default scope type, or null if none is specified
    */
   public Annotation getDefaultScopeType()
   {
      return defaultScopeType;
   }

   /**
    * Get any interceptor bindings the the stereotype specifies
    * 
    * @return The interceptor bindings, or an empty set if none are specified.
    */
   public Set<Annotation> getInterceptorBindings()
   {
      return interceptorBindings;
   }

   /**
    * Indicates if the bean name is defaulted
    * 
    * @return True if defaulted, false otherwise
    */
   public boolean isBeanNameDefaulted()
   {
      return beanNameDefaulted;
   }

   /**
    * Gets the meta-annotation type
    * 
    * @return The Stereotype class
    */
   protected Set<Class<? extends Annotation>> getMetaAnnotationTypes() 
   {
      return META_ANNOTATIONS;
   }

   /**
    * @return
    */
   public boolean isPolicy()
   {
      return policy;
   }
   
   public Set<Annotation> getInheritedSterotypes()
   {
      return inheritedSterotypes;
   }
   
   /**
    * @return the metaAnnotations
    */
   public Set<Annotation> getMetaAnnotations()
   {
      return metaAnnotations;
   }

}
