package org.jboss.weld.tests.producer.method.circular;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class CircularInjectionTest extends AbstractWeldTest
{

   @Test(description="WELD-310")
   public void testProducerCalledOnBeanUnderConstruction()
   {
      
   }
   
}
