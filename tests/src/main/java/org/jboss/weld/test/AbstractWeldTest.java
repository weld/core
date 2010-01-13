package org.jboss.weld.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.servlet.ServletContext;

import org.jboss.testharness.AbstractTest;
import org.jboss.testharness.impl.runner.servlet.ServletTestRunner;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.mock.el.EL;
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
