package org.jboss.weld.tests.producer.method;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class NullProducerTest extends AbstractWeldTest
{
   
   @Test(description="WBRI-276")
   public void testProducerMethodReturnsNull()
   {
      getReference(Government.class).destabilize();
   }

}
