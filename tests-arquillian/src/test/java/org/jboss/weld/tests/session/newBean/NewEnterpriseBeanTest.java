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
package org.jboss.weld.tests.session.newBean;

import java.util.Set;

import javax.enterprise.inject.New;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.weld.bean.NewSessionBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.introspector.WeldAnnotated;
import org.jboss.weld.literal.NewLiteral;
import org.jboss.weld.tests.category.Broken;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class NewEnterpriseBeanTest
{
   @Deployment
   public static Archive<?> deploy() 
   {
      return ShrinkWrap.create(BeanArchive.class)
                  .addPackage(NewEnterpriseBeanTest.class.getPackage())
                  .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
   }
   
   private static final New NEW_LITERAL = new NewLiteral()
   {
      @Override
      public java.lang.Class<?> value() 
      {
         return WrappedEnterpriseBean.class;
      }
      
   };

   @Inject 
   private BeanManager beanManager;
   
   private SessionBean<WrappedEnterpriseBeanLocal> wrappedEnterpriseBean;
   private NewSessionBean<WrappedEnterpriseBeanLocal> newEnterpriseBean;
   
   public void initNewBean() 
   {
      Set<Bean<?>> beans = beanManager.getBeans(WrappedEnterpriseBeanLocal.class);
      Assert.assertEquals(1, beanManager.getBeans(WrappedEnterpriseBeanLocal.class).size());
      Assert.assertTrue(beanManager.getBeans(WrappedEnterpriseBeanLocal.class).iterator().next() instanceof SessionBean<?>);
      wrappedEnterpriseBean = (SessionBean<WrappedEnterpriseBeanLocal>) beanManager.getBeans(WrappedEnterpriseBeanLocal.class).iterator().next();
      
      Assert.assertEquals(1, beanManager.getBeans(WrappedEnterpriseBeanLocal.class, NEW_LITERAL).size());
      Assert.assertTrue(beanManager.getBeans(WrappedEnterpriseBeanLocal.class, NEW_LITERAL).iterator().next() instanceof NewSessionBean<?>);
      newEnterpriseBean = (NewSessionBean<WrappedEnterpriseBeanLocal>) beanManager.getBeans(WrappedEnterpriseBeanLocal.class, NEW_LITERAL).iterator().next();
   }
   
   @Test
   @Category(Broken.class)
   public void testNewBeanHasImplementationClassOfInjectionPointType()
   {
      initNewBean();
      Assert.assertEquals(WrappedEnterpriseBean.class, newEnterpriseBean.getType());
   }

   @Test
   public void testNewBeanHasSameInitializerMethodsAsWrappedBean()
   {
      initNewBean();
      Assert.assertEquals(wrappedEnterpriseBean.getInitializerMethods(), newEnterpriseBean.getInitializerMethods());
   }

   @Test
   public void testNewBeanHasSameInjectedFieldsAsWrappedBean()
   {
      initNewBean();
      Set<? extends WeldAnnotated<?, ?>> wrappedBeanInjectionPoints = wrappedEnterpriseBean.getWeldInjectionPoints();
      Set<? extends WeldAnnotated<?, ?>> newBeanInjectionPoints = newEnterpriseBean.getWeldInjectionPoints();
      Assert.assertEquals(wrappedBeanInjectionPoints, newBeanInjectionPoints);
   }
   
}
