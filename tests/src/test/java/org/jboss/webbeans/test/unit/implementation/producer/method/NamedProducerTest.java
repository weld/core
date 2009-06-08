package org.jboss.webbeans.test.unit.implementation.producer.method;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class NamedProducerTest extends AbstractWebBeansTest
{
   @Test
   public void testNamedProducer()
   {
      String[] iemon = (String[]) getCurrentManager().getReference(getCurrentManager().getHighestPrecedenceBean(getCurrentManager().getBeans("iemon")), Object.class);
      assert iemon.length == 3;
      String[] itoen = (String[]) getCurrentManager().getReference(getCurrentManager().getHighestPrecedenceBean(getCurrentManager().getBeans("itoen")), Object.class);
      assert itoen.length == 2;
   }

}
