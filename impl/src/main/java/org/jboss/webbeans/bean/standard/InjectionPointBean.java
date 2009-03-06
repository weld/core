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

import javax.context.CreationalContext;
import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.literal.CurrentLiteral;

/**
 * Bean for InjectionPoint metadata
 * 
 * @author David Allen
 * 
 */
public class InjectionPointBean extends AbstractStandardBean<InjectionPoint>
{
   
   private static final Annotation[] DEFAULT_BINDING_ARRAY = { new CurrentLiteral() };
   private static final Set<Annotation> DEFAULT_BINDING = new HashSet<Annotation>(Arrays.asList(DEFAULT_BINDING_ARRAY));
   private static final Set<Type> TYPES = new HashSet<Type>(Arrays.asList(InjectionPoint.class));

   /**
    * Creates an InjectionPoint Web Bean for the injection of the containing bean owning
    * the field, constructor or method for the annotated item
    * 
    * @param <T> must be InjectionPoint
    * @param <S>
    * @param field The annotated member field/parameter for the injection
    * @param manager The RI manager implementation
    * @return a new bean for this injection point
    */
   public static InjectionPointBean of(ManagerImpl manager)
   {
      return new InjectionPointBean(AnnotatedClassImpl.of(InjectionPoint.class), manager);
   }

   protected InjectionPointBean(AnnotatedClass<InjectionPoint> clazz, ManagerImpl manager)
   {
      super(manager);
   }

   public InjectionPoint create(CreationalContext<InjectionPoint> creationalContext)
   {
      return getManager().getInjectionPoint();
   }
   
   public void destroy(InjectionPoint instance) 
   {
      
   }

   @Override
   public Class<InjectionPoint> getType()
   {
      return InjectionPoint.class;
   }

   @Override
   public Set<Type> getTypes()
   {
      return TYPES;
   }
   
}
