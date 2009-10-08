package org.jboss.weld.test.unit.environments.servlet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.AnnotationLiteral;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.mock.MockServletLifecycle;
import org.jboss.weld.mock.TestContainer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ServletEnvironmentTest
{
   
   private TestContainer container;
   private BeanManagerImpl manager;
   
   @BeforeClass
   public void beforeClass() throws Throwable
   {
      container = new TestContainer(new MockServletLifecycle(), Arrays.asList(Animal.class, DeadlyAnimal.class, DeadlySpider.class, DeadlyAnimal.class, Hound.class, HoundLocal.class, Salmon.class, ScottishFish.class, SeaBass.class, Sole.class, Spider.class, Tarantula.class, TarantulaProducer.class, Tuna.class), null);
      container.startContainer();
      container.ensureRequestActive();
      manager = container.getBeanManager();
   }
   
   @AfterClass(alwaysRun=true)
   public void afterClass() throws Exception
   {
      container.stopContainer();
      container = null;
      manager = null;
   }
   
   @Test
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
      
      assert beans.get(Tuna.class) instanceof ManagedBean;
      assert beans.get(Salmon.class) instanceof ManagedBean;
      assert beans.get(SeaBass.class) instanceof ManagedBean;
      assert beans.get(Sole.class) instanceof ManagedBean;
      manager.getInstanceByType(Sole.class, new AnnotationLiteral<Whitefish>() {}).ping();
   }
   
   @Test
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
      
      assert beans.get(TarantulaProducer.class) instanceof ManagedBean;
      manager.getInstanceByType(Tarantula.class, new AnnotationLiteral<Tame>() {}).ping();
   }
   
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
      assert classes.get(Hound.class) instanceof ManagedBean;
      manager.getInstanceByType(HoundLocal.class, new AnnotationLiteral<Tame>() {}).ping();
   }
   
}
