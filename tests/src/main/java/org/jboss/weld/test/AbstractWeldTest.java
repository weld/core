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
package org.jboss.weld.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.servlet.ServletContext;

import org.jboss.testharness.AbstractTest;
import org.jboss.testharness.impl.runner.servlet.ServletTestRunner;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.el.EL;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

public abstract class AbstractWeldTest extends AbstractTest
{
   
   private ServletContext servletContext;

   @Override
   @BeforeSuite
   public void beforeSuite(ITestContext context) throws Exception
   {
      if (!isInContainer())
      {
         getCurrentConfiguration().getExtraPackages().add(AbstractWeldTest.class.getPackage().getName());
         getCurrentConfiguration().getExtraPackages().add(EL.class.getPackage().getName());
         //getCurrentConfiguration().getExtraPackages().add(MockServletContext.class.getPackage().getName());
      }
      super.beforeSuite(context);
   }
   
   @Override
   @BeforeClass
   public void beforeClass() throws Throwable
   {
      super.beforeClass();
      if (isInContainer())
      {
         servletContext = ServletTestRunner.getCurrentServletContext();
      }
      
   }
   
   @Override
   @AfterClass
   public void afterClass() throws Exception
   {
      servletContext = null;
      super.afterClass();
   }

   protected BeanManagerImpl getCurrentManager()
   {
      return BeanManagerLocator.INSTANCE.locate();
   }

   public <T> Bean<T> getBean(Type beanType, Annotation... bindings)
   {
      return Utils.getBean(getCurrentManager(), beanType, bindings);
   }

   public <T> Set<Bean<T>> getBeans(Class<T> type, Annotation... bindings)
   {
      return Utils.getBeans(getCurrentManager(), type, bindings);
   }

   public <T> T getReference(Class<T> beanType, Annotation... bindings)
   {
      return Utils.getReference(getCurrentManager(), beanType, bindings);
   }
   
   public <T> T getReference(Bean<T> bean)
   {
      return Utils.getReference(getCurrentManager(), bean);
   }

   public <T> T evaluateValueExpression(String expression, Class<T> expectedType)
   {
      return Utils.evaluateValueExpression(getCurrentManager(), expression, expectedType);
   }
   
   protected String getPath(String viewId)
   {
      return getContextPath() + viewId;
   }

}
