package org.jboss.weld.test.unit.lookup;

import java.util.List;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class LookupInstanceTest extends AbstractWeldTest
{
  
   
   @Test
   public void testLookupInstance() throws Exception
   {
      assert createContextualInstance(new TypeLiteral<Instance<List<?>>>(){}.getRawType(), new DefaultLiteral()) == null; 
   }
   
}
