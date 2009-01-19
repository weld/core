package org.jboss.webbeans.test.examples;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.webbeans.Production;
import javax.webbeans.Standard;

import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.test.unit.AbstractTest;
import org.testng.annotations.Test;

public class Tests extends AbstractTest
{
   @Test
   public void testGameGenerator() throws Exception {
     setupGameGenerator();
     
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

   private void setupGameGenerator() throws NoSuchMethodException
   {
      SimpleBean<Game> gameBean = createSimpleBean(Game.class);
      SimpleBean<Generator> generatorBean = createSimpleBean(Generator.class);
      Method method = Generator.class.getDeclaredMethod("next");
      method.setAccessible(true);
      ProducerMethodBean<Integer> nextBean = createProducerMethod(method, generatorBean);
        
      manager.addBean(gameBean);
      manager.addBean(generatorBean);
      manager.addBean(nextBean);
   }
   
   @Test
   public void testMockSentenceTranslator() throws Exception {
      setupTextTranslator();
      
      manager.setEnabledDeploymentTypes(Arrays.asList(Standard.class, Production.class, Mock.class));
      
      new RunInDependentContext()
      {
         
         @Override
         protected void execute() throws Exception
         {
            TextTranslator tt2 = manager.getInstanceByType(TextTranslator.class);
            assert "Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet.".equals( tt2.translate("Hello world. How's tricks?") );
         }
         
      }.run();  
   }

   @Test
   public void testSentenceTranslator() throws Exception {
      setupTextTranslator();
      
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
   
   private void setupTextTranslator()
   {
      SimpleBean<SentenceParser> spBean = createSimpleBean(SentenceParser.class);
      SimpleBean<SentenceTranslator> stBean = createSimpleBean(SentenceTranslator.class);
      SimpleBean<MockSentenceTranslator> mstBean = createSimpleBean(MockSentenceTranslator.class);
      SimpleBean<TextTranslator> ttBean = createSimpleBean(TextTranslator.class);
      
      manager.addBean(spBean);
      manager.addBean(stBean);
      manager.addBean(mstBean);
      manager.addBean(ttBean);
   }
   
}
