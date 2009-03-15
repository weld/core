package org.jboss.webbeans.test.unit.bootstrap.environments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.AnnotationLiteral;
import javax.inject.manager.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.mock.MockEELifecycle;
import org.jboss.webbeans.mock.MockServletLifecycle;
import org.jboss.webbeans.test.unit.AbstractWebBeansTest;
import org.jboss.webbeans.test.unit.StandaloneContainersImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
      List<Bean<?>> beans = manager.getBeans();
      Map<Class<?>, Bean<?>> classes = new HashMap<Class<?>, Bean<?>>();
      for (Bean<?> bean : beans)
      {
         if (bean instanceof RIBean)
         {
            classes.put(((RIBean<?>) bean).getType(), bean);
         }
      }
      assert classes.containsKey(Tuna.class);
      assert classes.containsKey(Salmon.class);
      assert classes.containsKey(SeaBass.class);
      assert classes.containsKey(Sole.class);
      
      assert classes.get(Tuna.class) instanceof SimpleBean;
      assert classes.get(Salmon.class) instanceof SimpleBean;
      assert classes.get(SeaBass.class) instanceof SimpleBean;
      assert classes.get(Sole.class) instanceof SimpleBean;
      manager.getInstanceByType(Sole.class, new AnnotationLiteral<Whitefish>() {}).ping();
   }
   
   @Test(groups="incontainer-broken")
   public void testProducerMethodBean()
   {
      //deployBeans(TarantulaProducer.class);
      List<Bean<?>> beans = manager.getBeans();
      Map<Class<?>, Bean<?>> classes = new HashMap<Class<?>, Bean<?>>();
      for (Bean<?> bean : beans)
      {
         if (bean instanceof RIBean)
         {
            classes.put(((RIBean<?>) bean).getType(), bean);
         }
      }
      assert classes.containsKey(TarantulaProducer.class);
      assert classes.containsKey(Tarantula.class);
      
      assert classes.get(TarantulaProducer.class) instanceof SimpleBean;
      assert classes.get(Tarantula.class) instanceof ProducerMethodBean;
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
