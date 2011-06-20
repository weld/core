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
package org.jboss.weld.tests.managed.newBean;

import java.util.Set;

import javax.enterprise.inject.New;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.bean.NewManagedBean;
import org.jboss.weld.introspector.WeldAnnotated;
import org.jboss.weld.literal.NewLiteral;
import org.jboss.weld.util.AnnotatedTypes;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class NewSimpleBeanTest
{
   @Deployment
   public static Archive<?> deploy()
   {
      return ShrinkWrap.create(BeanArchive.class)
         .addPackage(NewSimpleBeanTest.class.getPackage());
   }

   private ManagedBean<WrappedSimpleBean> wrappedSimpleBean;
   private NewManagedBean<WrappedSimpleBean> newSimpleBean;

   private static final New NEW_LITERAL = new NewLiteral();

   @Inject
   private BeanManager beanManager;

   public void initNewBean() {

      Assert.assertEquals(1, beanManager.getBeans(WrappedSimpleBean.class).size());
      Assert.assertTrue(beanManager.getBeans(WrappedSimpleBean.class).iterator().next() instanceof ManagedBean);
      wrappedSimpleBean = (ManagedBean<WrappedSimpleBean>) beanManager.getBeans(WrappedSimpleBean.class).iterator().next();

      Assert.assertEquals(1, beanManager.getBeans(WrappedSimpleBean.class, NEW_LITERAL).size());
      Assert.assertTrue(beanManager.getBeans(WrappedSimpleBean.class, NEW_LITERAL).iterator().next() instanceof NewManagedBean);
      newSimpleBean = (NewManagedBean<WrappedSimpleBean>) beanManager.getBeans(WrappedSimpleBean.class, NEW_LITERAL).iterator().next();
   }

   // groups = { "new" }
   @Test
   public void testNewBeanHasImplementationClassOfInjectionPointType()
   {
      initNewBean();
      Assert.assertEquals(WrappedSimpleBean.class, newSimpleBean.getType());
   }

   // groups = { "new" }
   @Test
   public void testNewBeanIsSimpleWebBeanIfParameterTypeIsSimpleWebBean()
   {
      initNewBean();
      Assert.assertEquals(wrappedSimpleBean.getType(), newSimpleBean.getType());
   }

   // groups = { "new" }
   @Test
   public void testNewBeanHasSameConstructorAsWrappedBean()
   {
      initNewBean();
      Assert.assertTrue(AnnotatedTypes.compareAnnotatedCallable(wrappedSimpleBean.getConstructor(), newSimpleBean.getConstructor()));
   }

   // groups = { "new" }
   @Test
   public void testNewBeanHasSameInitializerMethodsAsWrappedBean()
   {
      initNewBean();
      Assert.assertEquals(wrappedSimpleBean.getInitializerMethods(), newSimpleBean.getInitializerMethods());
   }

   // groups = { "new" }
   @Test
   public void testNewBeanHasSameInjectedFieldsAsWrappedBean()
   {
      initNewBean();
      Set<? extends WeldAnnotated<?, ?>> wrappedBeanInjectionPoints = wrappedSimpleBean.getWeldInjectionPoints();
      Set<? extends WeldAnnotated<?, ?>> newBeanInjectionPoints = newSimpleBean.getWeldInjectionPoints();
      Assert.assertEquals(wrappedBeanInjectionPoints, newBeanInjectionPoints);
   }

}