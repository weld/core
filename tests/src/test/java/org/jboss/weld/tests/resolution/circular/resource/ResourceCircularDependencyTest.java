package org.jboss.weld.tests.resolution.circular.resource;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Resource;
import org.jboss.testharness.impl.packaging.Resources;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@IntegrationTest
@Resources({
   @Resource(source="persistence.xml", destination="WEB-INF/classes/META-INF/persistence.xml")
})
public class ResourceCircularDependencyTest extends AbstractWeldTest
{
   
   @Test
   public void testResourceProducerField() throws Exception
   {
      assert getReference(Baz.class).getFooDb().isOpen();
      assert true;
   }

}
