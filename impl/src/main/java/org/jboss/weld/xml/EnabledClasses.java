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
package org.jboss.weld.xml;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * 
 * @author Nicklas Karlsson
 *
 */
public class EnabledClasses
{
   private List<Class<? extends Annotation>> enabledPolicyStereotypes;
   private List<Class<?>> enabledPolicyClasses;
   private List<Class<?>> enabledDecoratorClasses;
   private List<Class<?>> enabledInterceptorClasses;
   private ResourceLoader resourceLoader;

   private EnabledClasses(MergedElements beanXmlElements, ResourceLoader resourceLoader)
   {
      enabledPolicyStereotypes = new ArrayList<Class<? extends Annotation>>();
      enabledPolicyClasses = new ArrayList<Class<?>>();
      enabledDecoratorClasses = new ArrayList<Class<?>>();
      enabledInterceptorClasses = new ArrayList<Class<?>>();
      this.resourceLoader = resourceLoader;
      process(beanXmlElements);
   }

   public static EnabledClasses of(MergedElements beanXmlElements, ResourceLoader resourceLoader)
   {
      return new EnabledClasses(beanXmlElements, resourceLoader);
   }

   private void process(MergedElements beanXmlElements)
   {
      processPolicies(beanXmlElements.getPoliciesElements());
      enabledDecoratorClasses.addAll(getClassesInElements(beanXmlElements.getDecoratorsElements()));
      enabledInterceptorClasses.addAll(getClassesInElements(beanXmlElements.getInterceptorsElements()));
   }

   private void processPolicies(List<BeansXmlElement> policyElements)
   {
      Collection<Class<?>> classes = getClassesInElements(policyElements);
      for (Class<?> clazz : classes) {
         if (clazz.isAnnotation()) {
            enabledPolicyStereotypes.add(clazz.asSubclass(Annotation.class));
         } else {
            enabledPolicyClasses.add(clazz);
         }
      }
   }
   
   private Collection<Class<?>> getClassesInElements(List<BeansXmlElement> elements)
   {
      LinkedHashSet<Class<?>> classes = new LinkedHashSet<Class<?>>();
      for (BeansXmlElement element : elements)
      {
         classes.addAll(element.getClasses(resourceLoader));
      }
      return classes;
   }

   public List<Class<? extends Annotation>> getEnabledPolicyStereotypes()
   {
      return enabledPolicyStereotypes;
   }

   public List<Class<?>> getEnabledPolicyClasses()
   {
      return enabledPolicyClasses;
   }

   public List<Class<?>> getEnabledDecoratorClasses()
   {
      return enabledDecoratorClasses;
   }

   public List<Class<?>> getEnabledInterceptorClasses()
   {
      return enabledInterceptorClasses;
   }
}
