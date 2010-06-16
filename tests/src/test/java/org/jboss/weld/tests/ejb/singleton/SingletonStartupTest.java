package org.jboss.weld.tests.ejb.singleton;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@IntegrationTest
@Packaging(PackagingType.EAR)
public class SingletonStartupTest extends AbstractWeldTest
{
 
   @Test(enabled=false) // JBAS-8107
   public void testSingletonStartup()
   {
      assert Foo.isPostConstructCalled();
   }

}
