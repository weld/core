package org.jboss.weld.tests.ejb.mdb;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
public class EJBTest extends AbstractWeldTest
{
   
   @Test
   public void testMdbDeploys()
   {
   }
   
}
