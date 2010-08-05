package org.jboss.weld.tests.resource.producer.non.dependent;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.ExpectedDeploymentException;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@ExpectedDeploymentException(DefinitionException.class)
public class NonDependentResourceProducerFieldTest extends AbstractWeldTest
{
   
   @Test(groups="incontainer-broken")
   // JBoss AS not reporting the right exception
   public void test()
   {
      assert false;
   }

}
