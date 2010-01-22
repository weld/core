/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.extensions.annotatedType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.util.AnnotationLiteral;

public class AnnotatedTypeExtension implements Extension
{
   
   public static class EcoFriendlyWashingMachineLiteral extends AnnotationLiteral<EcoFriendlyWashingMachine> implements EcoFriendlyWashingMachine
   {
      
      public static final EcoFriendlyWashingMachine INSTANCE = new EcoFriendlyWashingMachineLiteral();
      
   }
   
   /**
    * Adds an eco friendly wasing machine
    * @param beforeBeanDiscovery
    */
   public void addWashingMachine(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
   {
      beforeBeanDiscovery.addAnnotatedType(new AnnotatedType<WashingMachine>()
      {

         public Set<AnnotatedConstructor<WashingMachine>> getConstructors()
         {
            return Collections.emptySet();
         }

         public Set<AnnotatedField<? super WashingMachine>> getFields()
         {
            return Collections.emptySet();
         }

         public Class<WashingMachine> getJavaClass()
         {
            return WashingMachine.class;
         }

         public Set<AnnotatedMethod<? super WashingMachine>> getMethods()
         {
            return Collections.emptySet();
         }

         public <T extends Annotation> T getAnnotation(Class<T> annotationType)
         {
            if(annotationType == EcoFriendlyWashingMachine.class)
            {
               return annotationType.cast(EcoFriendlyWashingMachineLiteral.INSTANCE);
            }
            return null;
         }

         public Set<Annotation> getAnnotations()
         {
            return Collections.<Annotation>singleton(EcoFriendlyWashingMachineLiteral.INSTANCE);
         }

         public Type getBaseType()
         {
           return WashingMachine.class;
         }

         public Set<Type> getTypeClosure()
         {
            Set<Type> ret = new HashSet<Type>();
            ret.add(Object.class);
            ret.add(WashingMachine.class);
            return ret;
         }

         public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
         {
            return annotationType == EcoFriendlyWashingMachine.class;
         }
         
      });
   }
}
