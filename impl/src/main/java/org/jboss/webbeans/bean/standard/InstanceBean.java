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
package org.jboss.webbeans.bean.standard;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.TypeLiteral;
import javax.inject.Obtains;

import org.jboss.webbeans.InstanceImpl;
import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.injection.resolution.ResolvableTransformer;
import org.jboss.webbeans.literal.ObtainsLiteral;

public class InstanceBean extends AbstractFacadeBean<Instance<?>>
{

   private static final Class<Instance<?>> TYPE = new TypeLiteral<Instance<?>>() {}.getRawType();
   private static final Set<Type> DEFAULT_TYPES = new HashSet<Type>(Arrays.asList(TYPE, Object.class));
   private static final Obtains OBTAINS = new ObtainsLiteral();
   private static final Set<Annotation> DEFAULT_BINDINGS = new HashSet<Annotation>(Arrays.asList(OBTAINS));
   private static final Set<Class<? extends Annotation>> FILTERED_ANNOTATION_TYPES = new HashSet<Class<? extends Annotation>>(Arrays.asList(Obtains.class));
   public static final ResolvableTransformer TRANSFORMER = new FacadeBeanResolvableTransformer(TYPE, OBTAINS);
   
   
   public static AbstractFacadeBean<Instance<?>> of(BeanManagerImpl manager)
   {
      return new InstanceBean(manager);
   }
   
   protected InstanceBean(BeanManagerImpl manager)
   {
      super(manager);
   }

   @Override
   public Class<Instance<?>> getType()
   {
      return TYPE;
   }

   public Set<Type> getTypes()
   {
      return DEFAULT_TYPES;
   }
   
   @Override
   public Set<Annotation> getBindings()
   {
      return DEFAULT_BINDINGS;
   }

   @Override
   protected Instance<?> newInstance(Type type, Set<Annotation> annotations)
   {
      return InstanceImpl.of(type, getManager(), annotations);
   }

   @Override
   protected Set<Class<? extends Annotation>> getFilteredAnnotationTypes()
   {
      return FILTERED_ANNOTATION_TYPES;
   }
   
   @Override
   public String toString()
   {
      return "Built-in implicit javax.inject.Instance bean";
   }
   
}
