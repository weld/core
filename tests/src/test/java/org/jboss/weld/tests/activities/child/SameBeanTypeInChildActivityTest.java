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
package org.jboss.weld.tests.activities.child;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

/**
 * 
 * Spec version: 20090519
 *
 */
@Artifact
public class SameBeanTypeInChildActivityTest extends AbstractWeldTest
{
   private static final Set<Annotation> DEFAULT_QUALIFIERS = Collections.<Annotation>singleton(DefaultLiteral.INSTANCE);

   private Bean<?> createDummyBean(BeanManager beanManager)
   {
      final Set<InjectionPoint> injectionPoints = new HashSet<InjectionPoint>();
      final Set<Type> types = new HashSet<Type>();
      final Set<Annotation> bindings = new HashSet<Annotation>();
      bindings.add(new AnnotationLiteral<SpecialBindingType>() {});
      types.add(Object.class);
      final Bean<?> bean = new Bean<MyBean>()
      {

         public Set<Annotation> getQualifiers()
         {
            return bindings;
         }

         public Set<InjectionPoint> getInjectionPoints()
         {
            return injectionPoints;
         }

         public String getName()
         {
            return null;
         }

         public Class<? extends Annotation> getScope()
         {
            return Dependent.class;
         }

         public Set<Type> getTypes()
         {
            return types;
         }

         public boolean isNullable()
         {
            return false;
         }

         public MyBean create(CreationalContext<MyBean> creationalContext)
         {
            return null;
         }

         public void destroy(MyBean instance, CreationalContext<MyBean> creationalContext)
         {

         }

         public Class<?> getBeanClass()
         {
            return MyBean.class;
         }

         public boolean isAlternative()
         {
            return false;
         }

         public Set<Class<? extends Annotation>> getStereotypes()
         {
            return Collections.emptySet();
         }

      };
      return bean;
   }

   @Test(groups = { "broken" }, expectedExceptions = { InjectionException.class })
   public void testSameBeanTypeInChildAsParentInjection()
   {
      BeanManagerImpl childActivity = getCurrentManager().createActivity();
      Bean<?> anotherMyBean = createDummyBean(childActivity);
      childActivity.addBean(anotherMyBean);
   }

   @Test(groups = { "broken" }, expectedExceptions = { InjectionException.class })
   public void testSameBeanTypeInChildAsIndirectParentInjection()
   {
      BeanManagerImpl childActivity = getCurrentManager().createActivity();
      BeanManagerImpl grandChildActivity = childActivity.createActivity();
      Bean<?> anotherMyBean = createDummyBean(grandChildActivity);
      grandChildActivity.addBean(anotherMyBean);
   }
}
