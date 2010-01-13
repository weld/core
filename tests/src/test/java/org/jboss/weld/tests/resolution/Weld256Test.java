package org.jboss.weld.tests.resolution;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class Weld256Test extends AbstractWeldTest
{

   @Test
   public void testParameterizedInjection()
   {
      LookupFoo lookupFoo = getReference(LookupFoo.class);
      assert lookupFoo.getFoo().getName().equals("foo");
      assert lookupFoo.getFoobaz().getName().equals("foobase");
   }
   
}
