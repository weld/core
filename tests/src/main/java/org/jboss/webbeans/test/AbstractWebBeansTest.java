package org.jboss.webbeans.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.el.ELContext;
import javax.enterprise.inject.TypeLiteral;
import javax.enterprise.inject.spi.Bean;
import javax.servlet.ServletContext;

import org.jboss.testharness.AbstractTest;
import org.jboss.testharness.impl.runner.servlet.ServletTestRunner;
import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.mock.MockServletContext;
import org.jboss.webbeans.mock.el.EL;
import org.jboss.webbeans.servlet.ServletHelper;
import org.jboss.webbeans.util.collections.EnumerationList;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

public abstract class AbstractWebBeansTest extends AbstractTest
{
   
   private ServletContext servletContext;

   @Override
   @BeforeSuite
   public void beforeSuite(ITestContext context) throws Exception
   {
      if (!isInContainer())
      {
         getCurrentConfiguration().getExtraPackages().add(AbstractWebBeansTest.class.getPackage().getName());
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
      else
      {
         servletContext = new MockServletContext("");
      }
      
   }
   
   @Override
   @AfterClass
   public void afterClass() throws Exception
   {
      servletContext = null;
      super.afterClass();
   }

   /**
    * Checks if all annotations are in a given set of annotations
    * 
    * @param annotations The annotation set
    * @param annotationTypes The annotations to match
    * @return True if match, false otherwise
    */
   public boolean annotationSetMatches(Set<? extends Annotation> annotations, Class<? extends Annotation>... annotationTypes)
   {
      List<Class<? extends Annotation>> annotationTypeList = new ArrayList<Class<? extends Annotation>>();
      annotationTypeList.addAll(Arrays.asList(annotationTypes));
      for (Annotation annotation : annotations)
      {
         if (annotationTypeList.contains(annotation.annotationType()))
         {
            annotationTypeList.remove(annotation.annotationType());
         }
         else
         {
            return false;
         }
      }
      return annotationTypeList.size() == 0;
   }

   protected Iterable<URL> getResources(String name)
   {
      if (name.startsWith("/"))
      {
         name = name.substring(1);
      }
      else
      {
         name = getClass().getPackage().getName().replace(".", "/") + "/" + name;
      }
      try
      {
         return new EnumerationList<URL>(getClass().getClassLoader().getResources(name));
      }
      catch (IOException e)
      {
         throw new RuntimeException("Error loading resource from classloader" + name, e);
      }
   }

   protected byte[] serialize(Object instance) throws IOException
   {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bytes);
      out.writeObject(instance);
      return bytes.toByteArray();
   }

   protected Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException
   {
      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
      return in.readObject();
   }

   protected BeanManagerImpl getCurrentManager()
   {
      return ServletHelper.getModuleBeanManager(servletContext);
   }

   public boolean isExceptionInHierarchy(Throwable exception, Class<? extends Throwable> expectedException )
   {
      while (exception != null)
      {
         if (exception.getClass().equals(expectedException))
         {
            return true;
         }
         exception = exception.getCause();
      }
      return false;
   }

   public <T> Bean<T> getBean(Type beanType, Annotation... bindings)
   {
      Set<Bean<?>> beans = getCurrentManager().getBeans(beanType, bindings);
      if (beans.size() > 1)
      {
         throw new RuntimeException("More than one bean resolved to " + beanType + " with bindings " + Arrays.asList(bindings));
      }
      if (beans.size() == 0)
      {
         throw new RuntimeException("No beans resolved to " + beanType + " with bindings " + Arrays.asList(bindings));
      }
      @SuppressWarnings("unchecked")
      Bean<T> bean = (Bean<T>) beans.iterator().next();
      return bean;
   }

   @SuppressWarnings("unchecked")
   public <T> Set<Bean<T>> getBeans(Class<T> type, Annotation... bindings)
   {
      return (Set) getCurrentManager().getBeans(type, bindings);
   }

   @SuppressWarnings("unchecked")
   public <T> Set<Bean<T>> getBeans(TypeLiteral<T> type, Annotation... bindings)
   {
      return (Set)getCurrentManager().getBeans(type.getType(), bindings);
   }

   @SuppressWarnings("unchecked")
   public <T> T createContextualInstance(Class<T> beanType, Annotation... bindings)
   {
      return (T) createContextualInstance((Type) beanType, bindings);
   }

   public Object createContextualInstance(Type beanType, Annotation... bindings)
   {
      Bean<?> bean = getBean(beanType, bindings);
      return getCurrentManager().getReference(bean, beanType, getCurrentManager().createCreationalContext(bean));
   }

   @SuppressWarnings("unchecked")
   public <T> T evaluateValueExpression(String expression, Class<T> expectedType)
   {
      ELContext elContext = EL.createELContext(getCurrentManager().getCurrent());
      return (T) EL.EXPRESSION_FACTORY.createValueExpression(elContext, expression, expectedType).getValue(elContext);
   }
   
   protected String getPath(String viewId)
   {
      return getContextPath() + viewId;
   }

}
