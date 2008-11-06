package org.jboss.webbeans.examples;

import java.lang.reflect.Method;

import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.util.Util;
import org.testng.annotations.Test;

public class Tests extends AbstractTest
{
   @Test
   public void testGameGenerator() throws Exception {
     SimpleBean<Game> gameBean = Util.createSimpleWebBean(Game.class, manager);
     SimpleBean<Generator> generatorBean = Util.createSimpleWebBean(Generator.class, manager);
     Method method = Generator.class.getDeclaredMethod("next");
     method.setAccessible(true);
     ProducerMethodBean<Integer> nextBean = Util.createProducerMethodBean(int.class, method, manager, generatorBean);
     manager.addBean(gameBean);
     manager.addBean(generatorBean);
     manager.addBean(nextBean);
     Game game1 = manager.getInstanceByType(Game.class);
     Game game2 = manager.getInstanceByType(Game.class);
     assert game1!=game2;
     assert game1.getNumber()!=game2.getNumber();
     Generator gen1 = manager.getInstanceByType(Generator.class);
     Generator gen2 = manager.getInstanceByType(Generator.class);
     assert gen1.getRandom()!=null;
     assert gen1.getRandom()==gen2.getRandom();
   }
}
