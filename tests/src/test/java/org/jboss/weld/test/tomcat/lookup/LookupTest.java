package org.jboss.weld.test.tomcat.lookup;



import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Resource;
import org.jboss.testharness.impl.packaging.Resources;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact(addCurrentPackage=false)
@IntegrationTest
@Resources({
   @Resource(source="context.xml", destination="/META-INF/context.xml")
})
@Classes({Mouse.class, Vole.class, LookupTest.class})
public class LookupTest extends AbstractWeldTest
{
   
   @Test
   public void testManagerInJndi() throws Exception 
   {
      assert getCurrentManager().getInstanceByType(Mouse.class).getManager() != null;
      assert getCurrentManager().getInstanceByType(Mouse.class).getManager().equals(getCurrentManager());
   }
     
   @Test
   public void testResource() throws Exception 
   {
      assert getCurrentManager().getInstanceByType(Vole.class).getManager() != null;
      assert getCurrentManager().getInstanceByType(Vole.class).getManager().equals(getCurrentManager());
   }
   
}
