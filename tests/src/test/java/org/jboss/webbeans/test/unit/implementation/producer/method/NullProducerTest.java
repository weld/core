package org.jboss.webbeans.test.unit.implementation.producer.method;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class NullProducerTest extends AbstractWebBeansTest
{
   
   @Test(description="WBRI-276")
   public void testProducerMethodReturnsNull()
   {
      getCurrentManager().getInstanceByType(Government.class).destabilize();
   }

}
