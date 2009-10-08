package org.jboss.weld.test.unit.implementation.producer.method;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class ManagerProducerTest extends AbstractWebBeansTest
{
   @Test(description="WBRI-183")
   public void testInjectManagerProducer()
   {
      ManagerProducer.setInjectionPointInjected(false);
      getCurrentManager().getInstanceByType(IntInjection.class);
      assert ManagerProducer.isInjectionPointInjected();
   }

}
