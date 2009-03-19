package org.jboss.webbeans.test.unit.bootstrap.environments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.AnnotationLiteral;
import javax.inject.manager.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.mock.MockEELifecycle;
import org.jboss.webbeans.mock.MockServletLifecycle;
import org.jboss.webbeans.test.unit.AbstractWebBeansTest;
import org.jboss.webbeans.test.unit.StandaloneContainersImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

// TODO shouldn't extend AbstractWebBeansTest

@Artifact
public class ServletEnvironmentTest extends AbstractWebBeansTest
{
   
   @BeforeClass
   @Override
   public void beforeClass() throws Throwable
   {
      StandaloneContainersImpl.lifecycleClass = MockServletLifecycle.class;
      super.beforeClass();
   }
   
   @Override
   @AfterClass(alwaysRun=true)
   public void afterClass() throws Exception
   {
      StandaloneContainersImpl.lifecycleClass = MockEELifecycle.class;
      super.afterClass();
   }
   
   @Test(groups="incontainer-broken")
   public void testSimpleBeans()
   {
      Map<Class<?>, Bean<?>> beans = new HashMap<Class<?>, Bean<?>>();
      for (Bean<?> bean : manager.getBeans())
      {
         if (bean instanceof RIBean)
         {
            beans.put(((RIBean<?>) bean).getType(), bean);
         }
      }
      assert beans.containsKey(Tuna.class);
      assert beans.containsKey(Salmon.class);
      assert beans.containsKey(SeaBass.class);
      assert beans.containsKey(Sole.class);
      
      assert beans.get(Tuna.class) instanceof SimpleBean;
      assert beans.get(Salmon.class) instanceof SimpleBean;
      assert beans.get(SeaBass.class) instanceof SimpleBean;
      assert beans.get(Sole.class) instanceof SimpleBean;
      manager.getInstanceByType(Sole.class, new AnnotationLiteral<Whitefish>() {}).ping();
   }
   
   @Test(groups="incontainer-broken")
   public void testProducerMethodBean()
   {
      Map<Class<?>, Bean<?>> beans = new HashMap<Class<?>, Bean<?>>();
      for (Bean<?> bean : manager.getBeans())
      {
         if (bean instanceof RIBean)
         {
            beans.put(((RIBean<?>) bean).getType(), bean);
         }
      }
      assert beans.containsKey(TarantulaProducer.class);
      assert beans.containsKey(Tarantula.class);
      
      Bean<?> bean = beans.get(TarantulaProducer.class);
      
      assert beans.get(TarantulaProducer.class) instanceof SimpleBean;
      manager.getInstanceByType(Tarantula.class, new AnnotationLiteral<Tame>() {}).ping();
   }
   
   @Test(groups="incontainer-broken")
   public void testSingleEnterpriseBean()
   {
      List<Bean<?>> beans = manager.getBeans();
      Map<Class<?>, Bean<?>> classes = new HashMap<Class<?>, Bean<?>>();
      for (Bean<?> bean : beans)
      {
         if (bean instanceof RIBean)
         {
            classes.put(((RIBean<?>) bean).getType(), bean);
         }
      }
      assert classes.containsKey(Hound.class);
      assert classes.get(Hound.class) instanceof SimpleBean;
      manager.getInstanceByType(HoundLocal.class, new AnnotationLiteral<Tame>() {}).ping();
   }
   
}
