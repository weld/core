package org.jboss.weld.test.examples;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.jsr299.BeansXml;
import org.jboss.weld.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
@BeansXml("beans.xml")
public class MockExampleTest extends AbstractWebBeansTest
{
   
   @Test
   public void testMockSentenceTranslator() throws Exception 
   {   
      TextTranslator tt2 = getCurrentManager().getInstanceByType(TextTranslator.class);
      assert "Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet.".equals( tt2.translate("Hello world. How's tricks?") );
   }
   
}
