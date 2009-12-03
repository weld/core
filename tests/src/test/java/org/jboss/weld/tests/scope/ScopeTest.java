package org.jboss.weld.tests.scope;

import javax.enterprise.context.Dependent;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class ScopeTest extends AbstractWeldTest
{
   
   @Test(description="WELD-322")
   public void testScopeDeclaredOnSubclassOverridesScopeOnSuperClass()
   {
      assert getCurrentManager().resolve(getCurrentManager().getBeans(Bar.class)).getScope().equals(Dependent.class);
   }

}
