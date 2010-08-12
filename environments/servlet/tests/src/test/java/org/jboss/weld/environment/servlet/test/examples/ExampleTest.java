package org.jboss.weld.environment.servlet.test.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.environment.servlet.test.util.DeploymentDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ExampleTest
{

   @Deployment
   public static WebArchive deployment()
   {
      return DeploymentDescriptor.deployment().addPackage(ExampleTest.class.getPackage());
   }

   @Test
   public void testGameGenerator(Game game1, Game game2, Generator generator1, Generator generator2) throws Exception
   {
      assertNotNull(game1);
      assertNotNull(game2);
      assertNotSame(game1, game2);
      assertNotSame(game1.getNumber(), game2.getNumber());

      assertNotNull(generator1);
      assertNotNull(generator2);
      assertNotNull(generator1.getRandom());
      assertEquals(generator1.getRandom(), generator2.getRandom());
   }

   @Test(expected = UnsupportedOperationException.class)
   public void testSentenceTranslator(TextTranslator textTranslator)
   {
      textTranslator.translate("foo");
   }

}
