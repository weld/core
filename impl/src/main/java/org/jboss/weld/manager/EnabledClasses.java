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
package org.jboss.weld.manager;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author Nicklas Karlsson
 * 
 */
public class EnabledClasses
{

   private final List<Class<? extends Annotation>> alternativeStereotypes;
   private final List<Class<?>> alternativeClasses;
   private final List<Class<?>> decorators;
   private final List<Class<?>> interceptors;

   public EnabledClasses()
   {
      this.alternativeClasses = Collections.emptyList();
      this.alternativeStereotypes = Collections.emptyList();
      this.decorators = Collections.emptyList();
      this.interceptors = Collections.emptyList();
   }

   public EnabledClasses(List<Class<? extends Annotation>> alternativeStereotypes, List<Class<?>> alternativeClasses, List<Class<?>> decorators, List<Class<?>> interceptors)
   {
      this.alternativeStereotypes = new ArrayList<Class<? extends Annotation>>(alternativeStereotypes);
      this.alternativeClasses = new ArrayList<Class<?>>(alternativeClasses);
      this.decorators = new ArrayList<Class<?>>(decorators);
      this.interceptors = new ArrayList<Class<?>>(interceptors);
   }

   public List<Class<? extends Annotation>> getAlternativeStereotypes()
   {
      return Collections.unmodifiableList(alternativeStereotypes);
   }

   public List<Class<?>> getAlternativeClasses()
   {
      return Collections.unmodifiableList(alternativeClasses);
   }

   public List<Class<?>> getDecorators()
   {
      return Collections.unmodifiableList(decorators);
   }

   public List<Class<?>> getInterceptors()
   {
      return Collections.unmodifiableList(interceptors);
   }
}
