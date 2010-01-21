package org.jboss.weld.tests.scope.unproxyable;

import javax.enterprise.inject.UnproxyableResolutionException;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.ExpectedDeploymentException;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@ExpectedDeploymentException(UnproxyableResolutionException.class)
public class UnproxyableTest extends AbstractWeldTest
{
   
   @Test
   public void test()
   {
      
   }

}
