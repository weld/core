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
package org.jboss.webbeans.metadata.cache;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.context.ScopeType;
import javax.enterprise.inject.BindingType;
import javax.enterprise.inject.Named;
import javax.enterprise.inject.deployment.DeploymentType;
import javax.enterprise.inject.stereotype.Stereotype;
import javax.interceptor.InterceptorBindingType;

import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.resources.ClassTransformer;

/**
 * A meta model for a stereotype, allows us to cache a stereotype and to
 * validate it
 * 
 * @author Pete Muir
 * 
 */
public class StereotypeModel<T extends Annotation> extends AnnotationModel<T>
{
   // The default deployment type
   private Annotation defaultDeploymentType;
   // The default scope type
   private Annotation defaultScopeType;
   // Is the bean name defaulted
   private boolean beanNameDefaulted;
   // The supported scopes
   private Set<Class<? extends Annotation>> supportedScopes;
   // The required types
   private Set<Class<?>> requiredTypes;
   // The interceptor bindings
   private Set<Annotation> interceptorBindings;

   /**
    * Constructor
    * 
    * @param sterotype The stereotype
    */
   public StereotypeModel(Class<T> sterotype, ClassTransformer transformer)
   {
      super(sterotype, transformer);
      initDefaultDeploymentType();
      initDefaultScopeType();
      initBeanNameDefaulted();
      initInterceptorBindings();
      checkBindings();
   }

   /**
    * Validates the binding types
    */
   private void checkBindings()
   {
      Set<Annotation> bindings = getAnnotatedAnnotation().getMetaAnnotations(BindingType.class);
      if (bindings.size() > 0)
      {
         throw new DefinitionException("Cannot declare binding types on a stereotype " + getAnnotatedAnnotation());
      }
   }

   /**
    * Initializes the interceptor bindings
    */
   private void initInterceptorBindings()
   {
      interceptorBindings = getAnnotatedAnnotation().getMetaAnnotations(InterceptorBindingType.class);
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
      Set<Annotation> scopeTypes = getAnnotatedAnnotation().getMetaAnnotations(ScopeType.class);
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
   private void initDefaultDeploymentType()
   {
      Set<Annotation> deploymentTypes = getAnnotatedAnnotation().getMetaAnnotations(DeploymentType.class);
      if (deploymentTypes.size() > 1)
      {
         throw new DefinitionException("At most one deployment type may be specified on " + getAnnotatedAnnotation());
      }
      else if (deploymentTypes.size() == 1)
      {
         this.defaultDeploymentType = deploymentTypes.iterator().next();
      }
   }

   /**
    * Get the default deployment type the stereotype specifies
    * 
    * @return The default deployment type, or null if none is specified
    */
   public Annotation getDefaultDeploymentType()
   {
      return defaultDeploymentType;
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
    * Gets the supported scopes
    * 
    * @return A set of supported scopes, or an empty set if none are specified
    */
   public Set<Class<? extends Annotation>> getSupportedScopes()
   {
      return supportedScopes;
   }

   /**
    * Gets the required types
    * 
    * @return A set of required types, or an empty set if none are specified
    */
   public Set<Class<?>> getRequiredTypes()
   {
      return requiredTypes;
   }

   /**
    * Gets the type
    * 
    * @return The type
    */
   @Deprecated
   public Class<? extends Annotation> getStereotypeClass()
   {
      return getRawType();
   }

   /**
    * Gets a string representation of the stereotype
    * 
    * @return The string representation
    */
   @Override
   public String toString()
   {
     return "Stereotype model with required types " + requiredTypes + " and supported scopes " + supportedScopes; 
   }   

   /**
    * Gets the meta-annotation type
    * 
    * @return The Stereotype class
    */
   @Override
   protected Class<? extends Annotation> getMetaAnnotation()
   {
      return Stereotype.class;
   }

}
