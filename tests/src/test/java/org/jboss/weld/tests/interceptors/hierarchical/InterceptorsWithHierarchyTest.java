package org.jboss.weld.tests.interceptors.hierarchical;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.jsr299.BeansXml;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Marius Bogoevici
 */
@Artifact
@BeansXml("beans.xml")
public class InterceptorsWithHierarchyTest extends AbstractWeldTest
{
   @BeforeClass
   public static void initialize()
   {
      Defender.invocationsCount = 0;
   }

   @Test(groups = "broken")
   public void testInterceptorsWithHierarchy()
   {
      Attacker player = this.getReference(Attacker.class);
      player.cloneMe();
      assert Defender.invocationsCount == 1;
   }

}
