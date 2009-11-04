package org.jboss.weld.tests.contexts;

import java.util.List;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class ParameterizedTypeScopedTest extends AbstractWeldTest
{

   @Test
   public void testStringList()
   {
      List<String> str = createContextualInstance(StringHolder.class).getStrings();
      assert str.size() == 2;
   }
}
