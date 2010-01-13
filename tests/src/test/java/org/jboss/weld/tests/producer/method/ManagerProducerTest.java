package org.jboss.weld.tests.producer.method;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class ManagerProducerTest extends AbstractWeldTest
{
   @Test(description="WBRI-183")
   public void testInjectManagerProducer()
   {
      ManagerProducer.setInjectionPointInjected(false);
      getReference(IntInjection.class);
      assert ManagerProducer.isInjectionPointInjected();
   }

}
