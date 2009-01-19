package org.jboss.webbeans.test.unit;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import javax.webbeans.Production;
import javax.webbeans.Standard;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.NewEnterpriseBean;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.test.mock.MockBootstrap;
import org.jboss.webbeans.test.mock.MockEjbDescriptor;
import org.jboss.webbeans.test.mock.MockWebBeanDiscovery;
import org.testng.annotations.BeforeMethod;

public class AbstractTest
{
   
   protected abstract static class RunInDependentContext 
   {
      
      protected void setup()
      {
         AbstractTest.activateDependentContext();
      }
      
      protected void cleanup()
      {
         AbstractTest.deactivateDependentContext();
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
   
   protected ManagerImpl manager;
   protected MockBootstrap webBeansBootstrap;

   public static boolean visited = false;

   @BeforeMethod
   public void before() throws Exception
   {
      webBeansBootstrap = new MockBootstrap();
      manager = webBeansBootstrap.getManager();
      manager.setEnabledDeploymentTypes(getEnabledDeploymentTypes());
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
   
   protected <T> EnterpriseBean<T> createEnterpriseBean(Class<T> clazz)
   {
      manager.getEjbDescriptorCache().add(MockEjbDescriptor.of(clazz));
      return EnterpriseBean.of(clazz, manager);
   }
   
   protected <T> NewEnterpriseBean<T> createNewEnterpriseBean(Class<T> clazz)
   {
      manager.getEjbDescriptorCache().add(MockEjbDescriptor.of(clazz));
      return NewEnterpriseBean.of(clazz, manager);
   }

   protected static void activateDependentContext()
   {
      DependentContext.INSTANCE.setActive(true);
   }
   
   protected static void deactivateDependentContext()
   {
      DependentContext.INSTANCE.setActive(false);
   }
   
   protected ManagerImpl deploy(Class<?>... classes)
   {
      MockBootstrap bootstrap = new MockBootstrap();
      bootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(classes));
      bootstrap.boot();
      return bootstrap.getManager();
   }
}
