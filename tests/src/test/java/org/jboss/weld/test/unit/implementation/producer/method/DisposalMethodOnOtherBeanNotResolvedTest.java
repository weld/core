package org.jboss.weld.test.unit.implementation.producer.method;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class DisposalMethodOnOtherBeanNotResolvedTest extends AbstractWebBeansTest
{
   @Test
   public void test()
   {
      FooDisposer.reset();
      FooProducer.reset();
      Bean<Foo> bean = getBean(Foo.class);
      CreationalContext<Foo> ctx = getCurrentManager().createCreationalContext(bean);
      Foo instance = bean.create(ctx);
      assert instance.getBlah().equals("foo!");
      bean.destroy(instance, ctx);
      assert !FooDisposer.isDisposed();
      assert FooProducer.isDisposed();
   }

}
