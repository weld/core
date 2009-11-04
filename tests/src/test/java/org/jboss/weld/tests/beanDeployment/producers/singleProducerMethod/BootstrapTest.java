package org.jboss.weld.tests.beanDeployment.producers.singleProducerMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.bean.ProducerMethod;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
public class BootstrapTest extends AbstractWeldTest
{
   
   @Test(groups="bootstrap")
   public void testProducerMethodBean()
   {
      //deployBeans(TarantulaProducer.class);
      List<Bean<?>> beans = getCurrentManager().getBeans();
      Map<Class<?>, Bean<?>> classes = new HashMap<Class<?>, Bean<?>>();
      for (Bean<?> bean : beans)
      {
         if (bean instanceof RIBean<?>)
         {
            classes.put(((RIBean<?>) bean).getType(), bean);
         }
      }
      assert classes.containsKey(TarantulaProducer.class);
      assert classes.containsKey(Tarantula.class);
      
      assert classes.get(TarantulaProducer.class) instanceof ManagedBean<?>;
      assert classes.get(Tarantula.class) instanceof ProducerMethod<?, ?>;
   }
   
}
