package org.jboss.weld.tests.producer.method;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class DisposalMethodInjectionPointTest extends AbstractWeldTest
{
   @Test(groups = { "broken" })
   // WELD-358
   public void test()
   {
      BarProducer.reset();
      Bean<BarConsumer> barConsumerBean = getBean(BarConsumer.class);
      CreationalContext<BarConsumer> ctx = getCurrentManager().createCreationalContext(barConsumerBean);
      BarConsumer barConsumer = barConsumerBean.create(ctx);
      assert BarProducer.getProducedInjection().getName().equals("bar");
      Bar bar = barConsumer.getBar();
      barConsumerBean.destroy(barConsumer, ctx);
      assert BarProducer.getDisposedBar() == bar;
      assert BarProducer.getDisposedInjection().getName().equals("bar");
   }
}
