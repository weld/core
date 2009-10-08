package org.jboss.weld.test.examples;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class ExampleTest extends AbstractWeldTest
{
   @Test
   public void testGameGenerator() throws Exception 
   {
     Game game1 = getCurrentManager().getInstanceByType(Game.class);
     Game game2 = getCurrentManager().getInstanceByType(Game.class);
     assert game1!=game2;
     assert game1.getNumber()!=game2.getNumber();
     Generator gen1 = getCurrentManager().getInstanceByType(Generator.class);
     Generator gen2 = getCurrentManager().getInstanceByType(Generator.class);
     assert gen1.getRandom()!=null;
     assert gen1.getRandom()==gen2.getRandom();
   }

   @Test
   public void testSentenceTranslator() throws Exception 
   {
      
      TextTranslator tt1 = getCurrentManager().getInstanceByType(TextTranslator.class);
      try 
      {
         tt1.translate("hello world");
         assert false;
      }
      catch (UnsupportedOperationException uoe)
      {
         //expected
      }
   }
   
}
