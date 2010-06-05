/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
import java.util.Collections;
import java.util.List;

import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * 
 * @author Nicklas Karlsson
 * 
 */
public class EnabledClasses
{
   
   private final List<Class<? extends Annotation>> enabledAlternativeStereotypes;
   private final List<Class<?>> enabledAlternativeClasses;
   private final List<Class<?>> enabledDecoratorClasses;
   private final List<Class<?>> enabledInterceptorClasses;

   public EnabledClasses()
   {
      this.enabledAlternativeClasses = new ArrayList<Class<?>>();
      this.enabledAlternativeStereotypes = new ArrayList<Class<? extends Annotation>>();
      this.enabledDecoratorClasses = new ArrayList<Class<?>>();
      this.enabledInterceptorClasses = new ArrayList<Class<?>>();
   }
   
   EnabledClasses(ResourceLoader resourceLoader, MergedElements beanXmlElements)
   {
      this();
      for (BeansXmlElement element : beanXmlElements.getAlternativesElements())
      {
         for (Class<?> clazz : element.getClasses(resourceLoader))
         {
            if (clazz.isAnnotation())
            {
               this.enabledAlternativeStereotypes.add(clazz.asSubclass(Annotation.class));
            }
            else
            {
               this.enabledAlternativeClasses.add(clazz);
            }
         }
      }
      for (BeansXmlElement element : beanXmlElements.getDecoratorsElements())
      {
         this.enabledDecoratorClasses.addAll(element.getClasses(resourceLoader));
      }
      for (BeansXmlElement element : beanXmlElements.getInterceptorsElements())
      {
         this.enabledInterceptorClasses.addAll(element.getClasses(resourceLoader));
      }
   }

   public List<Class<? extends Annotation>> getEnabledAlternativeStereotypes()
   {
      return Collections.unmodifiableList(enabledAlternativeStereotypes);
   }

   public List<Class<?>> getEnabledAlternativeClasses()
   {
      return Collections.unmodifiableList(enabledAlternativeClasses);
   }

   public List<Class<?>> getEnabledDecoratorClasses()
   {
      return Collections.unmodifiableList(enabledDecoratorClasses);
   }

   public List<Class<?>> getEnabledInterceptorClasses()
   {
      return Collections.unmodifiableList(enabledInterceptorClasses);
   }
}
