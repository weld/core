package org.jboss.webbeans.test.unit.implementation.producer.method;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.unit.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class NamedProducerTest extends AbstractWebBeansTest
{
   @Test
   public void testNamedProducer()
   {
      String[] iemon = (String[]) manager.getInstanceByName("iemon");
      assert iemon.length == 3;
      String[] itoen = (String[]) manager.getInstanceByName("itoen");
      assert itoen.length == 2;
   }

}
