package org.jboss.webbeans.test.examples;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.unit.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class ExampleTest extends AbstractWebBeansTest
{
   @Test
   public void testGameGenerator() throws Exception {
     
     new RunInDependentContext()
     {
        
        @Override
        protected void execute() throws Exception
        {
           Game game1 = manager.getInstanceByType(Game.class);
           Game game2 = manager.getInstanceByType(Game.class);
           assert game1!=game2;
           assert game1.getNumber()!=game2.getNumber();
           Generator gen1 = manager.getInstanceByType(Generator.class);
           Generator gen2 = manager.getInstanceByType(Generator.class);
           assert gen1.getRandom()!=null;
           assert gen1.getRandom()==gen2.getRandom();
        }
        
     }.run();
   }

   @Test
   public void testSentenceTranslator() throws Exception {
      
      new RunInDependentContext()
      {
         
         @Override
         protected void execute() throws Exception
         {
            TextTranslator tt1 = manager.getInstanceByType(TextTranslator.class);
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
         
      }.run();
   }
   
}
