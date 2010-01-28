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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.jsr299.Extension;
import org.jboss.weld.test.AbstractWeldTest;
import org.jboss.weld.test.Utils;
import org.jboss.weld.tests.extensions.annotatedType.EcoFriendlyWashingMachine.EcoFriendlyWashingMachineLiteral;
import org.testng.annotations.Test;

@Artifact
@IntegrationTest
@Extension("javax.enterprise.inject.spi.Extension")
public class AnnotatedTypeExtensionTest extends AbstractWeldTest
{
   
   @Test
   public void testMultipleBeansOfSameType()
   {
      Laundry laundry = getReference(Laundry.class);
      assert laundry.ecoFriendlyWashingMachine != null;
      assert laundry.fastWashingMachine != null;
   }
   
   @Test(description = "WELD-371")
   public void testAnnotationsAreOverridden()
   {
      Bean<WashingMachine> bean = getBean(WashingMachine.class, EcoFriendlyWashingMachineLiteral.INSTANCE);
      assert Utils.annotationSetMatches(bean.getQualifiers(), Any.class, EcoFriendlyWashingMachine.class);
      
      // Verify overriding the class structure works
      Clothes.reset();
      TumbleDryer tumbleDryer = getReference(TumbleDryer.class);
      Bean<TumbleDryer> tumbleDryerBean = getBean(TumbleDryer.class);
      assert tumbleDryer != null;
      
      assert !containsConstructor(tumbleDryerBean.getInjectionPoints(), SerialNumber.class);
      assert containsConstructor(tumbleDryerBean.getInjectionPoints(), Clothes.class);
      assert tumbleDryer.getSerialNumber() == null;
      assert tumbleDryer.getClothes() != null;
      assert !Clothes.getInjectionPoint().getAnnotated().isAnnotationPresent(Original.class);
      AnnotatedConstructor<?> clothesConstructor = getConstructor(tumbleDryerBean.getInjectionPoints(), Clothes.class); 
      assert clothesConstructor.getParameters().get(0).isAnnotationPresent(Special.class);
      assert !clothesConstructor.getParameters().get(0).isAnnotationPresent(Original.class);
     
      assert containsField(tumbleDryerBean.getInjectionPoints(), "plug");
      assert !containsField(tumbleDryerBean.getInjectionPoints(), "coins");
      assert tumbleDryer.getPlug() != null;
      assert tumbleDryer.getCoins() == null;
      
      assert containsMethod(tumbleDryerBean.getInjectionPoints(), "setRunningTime", RunningTime.class);
      assert !containsMethod(tumbleDryerBean.getInjectionPoints(), "setHotAir", HotAir.class);
      assert tumbleDryer.getRunningTime() != null;
      assert tumbleDryer.getHotAir() == null;
      AnnotatedMethod<?> runningTimeMethod = getMethod(tumbleDryerBean.getInjectionPoints(), "setRunningTime", RunningTime.class);
      assert runningTimeMethod.getParameters().get(0).isAnnotationPresent(Special.class);
      assert !runningTimeMethod.getParameters().get(0).isAnnotationPresent(Original.class);
   }
   
   private static boolean containsField(Set<InjectionPoint> injectionPoints, String name)
   {
      for (InjectionPoint ip : injectionPoints)
      {
         if (ip.getAnnotated() instanceof AnnotatedField<?>)
         {
            AnnotatedField<?> field = (AnnotatedField<?>) ip.getAnnotated();
            if (field.getJavaMember().getName().equals(name))
            {
               return true;
            }
         }
      }
      return false;
   }
   
   private static boolean containsConstructor(Set<InjectionPoint> injectionPoints, Class<?>... parameters)
   {
      return getConstructor(injectionPoints, parameters) != null;
   }
   
   private static AnnotatedConstructor<?> getConstructor(Set<InjectionPoint> injectionPoints, Class<?>... parameters)
   {
      for (InjectionPoint ip : injectionPoints)
      {
         if (ip.getAnnotated() instanceof AnnotatedParameter<?>)
         {
            AnnotatedParameter<?> param = (AnnotatedParameter<?>) ip.getAnnotated();
            if (param.getDeclaringCallable() instanceof AnnotatedConstructor<?>)
            {
               Class<?>[] parameterTypes = ((Constructor<?>) param.getDeclaringCallable().getJavaMember()).getParameterTypes();
               if (Arrays.equals(parameters, parameterTypes))
               {
                  return (AnnotatedConstructor<?>) param.getDeclaringCallable();
               }
            }
         }
      }
      return null;
   }
   
   private static boolean containsMethod(Set<InjectionPoint> injectionPoints, String name, Class<?>... parameters)
   {
      return getMethod(injectionPoints, name, parameters) != null;
   }
   
   private static AnnotatedMethod<?> getMethod(Set<InjectionPoint> injectionPoints, String name, Class<?>... parameters)
   {
      for (InjectionPoint ip : injectionPoints)
      {
         if (ip.getAnnotated() instanceof AnnotatedParameter<?>)
         {
            AnnotatedParameter<?> param = (AnnotatedParameter<?>) ip.getAnnotated();
            if (param.getDeclaringCallable() instanceof AnnotatedMethod<?>)
            {
               Class<?>[] parameterTypes = ((Method) param.getDeclaringCallable().getJavaMember()).getParameterTypes();
               String methodName = param.getDeclaringCallable().getJavaMember().getName();
               if (Arrays.equals(parameters, parameterTypes) && methodName.equals(name))
               {
                  return (AnnotatedMethod<?>) param.getDeclaringCallable();
               }
            }
         }
      }
      return null;
   }
   

}
