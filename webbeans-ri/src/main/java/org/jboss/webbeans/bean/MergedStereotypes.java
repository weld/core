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

package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.jlr.AbstractAnnotatedItem.AnnotationMap;
import org.jboss.webbeans.model.StereotypeModel;

/**
 * Meta model for the merged stereotype for a bean
 * 
 * @author pmuir
 * 
 */
public class MergedStereotypes<T, E>
{
   private AnnotationMap possibleDeploymentTypes;
   private Set<Annotation> possibleScopeTypes;
   private boolean beanNameDefaulted;
   private Set<Class<?>> requiredTypes;
   private Set<Class<? extends Annotation>> supportedScopes;

   /**
    * Constructor
    * 
    * @param stereotypeAnnotations The stereotypes to merge
    * @param manager The Web Beans manager
    */
   public MergedStereotypes(Set<Annotation> stereotypeAnnotations, ManagerImpl manager)
   {
      possibleDeploymentTypes = new AnnotationMap();
      possibleScopeTypes = new HashSet<Annotation>();
      requiredTypes = new HashSet<Class<?>>();
      supportedScopes = new HashSet<Class<? extends Annotation>>();
      merge(stereotypeAnnotations, manager);
   }

   /**
    * Perform the merge
    * 
    * @param stereotypeAnnotations The stereotype annotations
    * @param manager The Web Beans manager
    */
   protected void merge(Set<Annotation> stereotypeAnnotations, ManagerImpl manager)
   {
      for (Annotation stereotypeAnnotation : stereotypeAnnotations)
      {
         // Retrieve and merge all metadata from stereotypes
         StereotypeModel<?> stereotype = manager.getMetaDataCache().getStereotype(stereotypeAnnotation.annotationType());
         if (stereotype == null)
         {
            throw new IllegalStateException("Stereotype " + stereotypeAnnotation + " not registered with container");
         }
         if (stereotype.getDefaultDeploymentType() != null)
         {
            possibleDeploymentTypes.put(stereotype.getDefaultDeploymentType().annotationType(), stereotype.getDefaultDeploymentType());
         }
         if (stereotype.getDefaultScopeType() != null)
         {
            possibleScopeTypes.add(stereotype.getDefaultScopeType());
         }
         requiredTypes.addAll(stereotype.getRequiredTypes());
         supportedScopes.addAll(stereotype.getSupportedScopes());
         if (stereotype.isBeanNameDefaulted())
         {
            beanNameDefaulted = true;
         }
      }
   }

   /**
    * Returns the possible deployment typess
    * 
    * @return The deployment types
    */
   public AnnotationMap getPossibleDeploymentTypes()
   {
      return possibleDeploymentTypes;
   }

   /**
    * Returns the possible scope types
    * 
    * @return The scope types
    */
   public Set<Annotation> getPossibleScopeTypes()
   {
      return possibleScopeTypes;
   }

   /**
    * Indicates if the name i defaulted
    * 
    * @return True if defaulted, false if not
    */
   public boolean isBeanNameDefaulted()
   {
      return beanNameDefaulted;
   }

   /**
    * Returns the required types
    * 
    * @return The required types
    */
   public Set<Class<?>> getRequiredTypes()
   {
      return requiredTypes;
   }

   /**
    * Returns the supported scopes
    * 
    * @return The supported scopes
    */
   public Set<Class<? extends Annotation>> getSupportedScopes()
   {
      return supportedScopes;
   }

   /**
    * Indicates if the bean was declared in XML
    * 
    * @return True if declared in XML, else false
    */
   public boolean isDeclaredInXml()
   {
      return false;
   }

   @Override
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("Merged stereotypes:\n");
      buffer.append("Bean name defaulted: " + beanNameDefaulted + "\n");
      buffer.append("Possible deployment types: " + possibleDeploymentTypes.size() + "\n");
      int i = 0;
      for (Entry<Class<? extends Annotation>, Annotation> entry : possibleDeploymentTypes.entrySet())
      {
         buffer.append(++i + " - " + entry.getKey().getName() + ": " + entry.getValue().toString() + "\n");
      }
      buffer.append("Possible scope types: " + possibleScopeTypes.size() + "\n");
      i = 0;
      for (Annotation scopeType : possibleScopeTypes)
      {
         buffer.append(++i + " - " + scopeType.toString());
      }
      buffer.append("Required types: " + requiredTypes.size() + "\n");
      i = 0;
      for (Class<?> requiredType : requiredTypes)
      {
         buffer.append(++i + " - " + requiredType.getName() + "\n");
      }
      buffer.append("Supported scopes: " + supportedScopes.size() + "\n");
      i = 0;
      for (Class<?> supportedScope : supportedScopes)
      {
         buffer.append(++i + " - " + supportedScope.getName() + "\n");
      }
      return buffer.toString();
   }

}
