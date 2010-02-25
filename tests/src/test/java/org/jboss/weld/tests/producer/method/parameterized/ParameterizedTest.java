package org.jboss.weld.tests.producer.method.parameterized;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class ParameterizedTest extends AbstractWeldTest
{

   @Test(description = "WELD-452")
   public void testEventQualifiersCorrect()
   {
      TestBean testBean = getReference(TestBean.class);
      assert testBean != null;
   }

}
