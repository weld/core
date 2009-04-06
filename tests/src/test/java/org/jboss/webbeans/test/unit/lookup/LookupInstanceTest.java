package org.jboss.webbeans.test.unit.lookup;

import java.util.List;

import javax.inject.Instance;
import javax.inject.TypeLiteral;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.literal.ObtainsLiteral;
import org.jboss.webbeans.test.unit.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class LookupInstanceTest extends AbstractWebBeansTest
{
  
   
   @Test
   public void testLookupInstance() throws Exception
   {
      assert manager.getInstanceByType(new TypeLiteral<Instance<List<?>>>(){}, new ObtainsLiteral()) == null; 
   }
   
}
