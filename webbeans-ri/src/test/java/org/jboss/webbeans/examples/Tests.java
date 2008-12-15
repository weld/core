package org.jboss.webbeans.examples;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.webbeans.Production;
import javax.webbeans.Standard;

import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.contexts.DependentContext;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.util.BeanFactory;
import org.testng.annotations.Test;

public class Tests extends AbstractTest
{
   @Test
   public void testGameGenerator() throws Exception {
     setupGameGenerator();
     
     try
     {
        DependentContext.INSTANCE.setActive(true);
        Game game1 = manager.getInstanceByType(Game.class);
        Game game2 = manager.getInstanceByType(Game.class);
        assert game1!=game2;
        assert game1.getNumber()!=game2.getNumber();
        Generator gen1 = manager.getInstanceByType(Generator.class);
        Generator gen2 = manager.getInstanceByType(Generator.class);
        assert gen1.getRandom()!=null;
        assert gen1.getRandom()==gen2.getRandom();
     }
     finally
     {
        DependentContext.INSTANCE.setActive(false);
     }
   }

   private void setupGameGenerator() throws NoSuchMethodException
   {
      SimpleBean<Game> gameBean = BeanFactory.createSimpleBean(Game.class, manager);
      SimpleBean<Generator> generatorBean = BeanFactory.createSimpleBean(Generator.class, manager);
      Method method = Generator.class.getDeclaredMethod("next");
      method.setAccessible(true);
      ProducerMethodBean<Integer> nextBean = BeanFactory.createProducerMethodBean(int.class, method, generatorBean, manager);
        
      manager.addBean(gameBean);
      manager.addBean(generatorBean);
      manager.addBean(nextBean);
   }
   
   @Test
   public void testMockSentenceTranslator() throws Exception {
      setupTextTranslator();
      
      manager.setEnabledDeploymentTypes(Arrays.asList(Standard.class, Production.class, Mock.class));
      
      try
      {
         DependentContext.INSTANCE.setActive(true);
         TextTranslator tt2 = manager.getInstanceByType(TextTranslator.class);
         assert "Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet.".equals( tt2.translate("Hello world. How's tricks?") );
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }      
   }

   @Test
   public void testSentenceTranslator() throws Exception {
      setupTextTranslator();
      
      try
      {
         DependentContext.INSTANCE.setActive(true);
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
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }
   
   private void setupTextTranslator()
   {
      SimpleBean<SentenceParser> spBean = BeanFactory.createSimpleBean(SentenceParser.class, manager);
      SimpleBean<SentenceTranslator> stBean = BeanFactory.createSimpleBean(SentenceTranslator.class, manager);
      SimpleBean<MockSentenceTranslator> mstBean = BeanFactory.createSimpleBean(MockSentenceTranslator.class, manager);
      SimpleBean<TextTranslator> ttBean = BeanFactory.createSimpleBean(TextTranslator.class, manager);
      
      manager.addBean(spBean);
      manager.addBean(stBean);
      manager.addBean(mstBean);
      manager.addBean(ttBean);
   }
   
}
