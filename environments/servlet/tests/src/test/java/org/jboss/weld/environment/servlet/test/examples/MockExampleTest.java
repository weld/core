package org.jboss.weld.environment.servlet.test.examples;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.environment.servlet.test.util.BeansXml;
import org.jboss.weld.environment.servlet.test.util.DeploymentDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class MockExampleTest
{
   
   @Deployment
   public static WebArchive deployment()
   {
      return DeploymentDescriptor.deployment(new BeansXml().alternatives(MockSentenceTranslator.class)).addPackage(MockExampleTest.class.getPackage());
   }
   
   @Test
   public void testMockSentenceTranslator(TextTranslator textTranslator) throws Exception 
   {   
      assert "Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet.".equals( textTranslator.translate("Hello world. How's tricks?") );
   }
   
}
