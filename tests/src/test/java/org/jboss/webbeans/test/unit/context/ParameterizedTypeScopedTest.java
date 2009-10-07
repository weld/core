package org.jboss.webbeans.test.unit.context;

import java.util.List;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class ParameterizedTypeScopedTest extends AbstractWebBeansTest
{

   @Test
   public void testStringList()
   {
      List<String> str = createContextualInstance(StringHolder.class).getStrings();
      assert str.size() == 2;
   }
}
