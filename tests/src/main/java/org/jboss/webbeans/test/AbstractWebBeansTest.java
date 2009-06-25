package org.jboss.webbeans.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.el.ELContext;
import javax.enterprise.inject.TypeLiteral;
import javax.enterprise.inject.deployment.Production;
import javax.enterprise.inject.deployment.Standard;
import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.AbstractTest;
import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.mock.el.EL;
import org.jboss.webbeans.util.EnumerationIterable;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

public abstract class AbstractWebBeansTest extends AbstractTest
{

   protected abstract static class RunInDependentContext 
   {

      protected void setup()
      {
         DependentContext.instance().setActive(true);
      }

      protected void cleanup()
      {
         DependentContext.instance().setActive(false);
      }

      public final void run() throws Exception
      {
         try
         {
            setup();
            execute();
         }
         finally
         {
            cleanup();
         }
      }

      protected abstract void execute() throws Exception;

   }

   protected static final int BUILT_IN_BEANS = 3;

   private BeanManagerImpl manager;

   public static boolean visited = false;

   @Override
   @BeforeSuite
   public void beforeSuite(ITestContext context) throws Exception
   {
      if (!isInContainer())
      {
         getCurrentConfiguration().getExtraPackages().add(AbstractWebBeansTest.class.getPackage().getName());
         getCurrentConfiguration().getExtraPackages().add(EL.class.getPackage().getName());
      }
      super.beforeSuite(context);
   }

   @BeforeMethod
   public void before() throws Exception
   {
      this.manager = CurrentManager.rootManager();
   }

   @AfterMethod
   public void after() throws Exception
   {
      this.manager = null;
   }


   protected List<Class<? extends Annotation>> getEnabledDeploymentTypes()
   {
      return getDefaultDeploymentTypes();
   }

   @SuppressWarnings("unchecked")
   protected final List<Class<? extends Annotation>> getDefaultDeploymentTypes()
   {
      return Arrays.asList(Standard.class, Production.class);
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
         return new EnumerationIterable<URL>(getClass().getClassLoader().getResources(name));
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
      return manager;
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
      ELContext elContext = EL.createELContext();
      return (T) EL.EXPRESSION_FACTORY.createValueExpression(elContext, expression, expectedType).getValue(elContext);
   }

}
