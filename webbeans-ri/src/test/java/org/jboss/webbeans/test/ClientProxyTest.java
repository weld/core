package org.jboss.webbeans.test;

import java.io.IOException;

import javax.webbeans.DefinitionException;
import javax.webbeans.UnproxyableDependencyException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.bean.BeanFactory;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.test.beans.Fox;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.test.beans.TunedTuna;
import org.jboss.webbeans.test.beans.broken.FinalTuna;
import org.jboss.webbeans.util.Reflections;
import org.testng.annotations.Test;

@SpecVersion("20081206")
public class ClientProxyTest extends AbstractTest
{

   @Test(groups = "Reflections")
   @SpecAssertion(section = { "5.4" })
   public void testClientProxyUsedForNormalScope()
   {
      Bean<Tuna> tunaBean = BeanFactory.createSimpleBean(Tuna.class, manager);
      manager.addBean(tunaBean);
      Tuna tuna = manager.getInstance(tunaBean);
      assert Reflections.isProxy(tuna);
   }

   @Test(groups = "Reflections")
   @SpecAssertion(section = { "5.4" })
   public void testClientProxyNotUsedForPseudoScope()
   {
      Bean<Fox> foxBean = BeanFactory.createSimpleBean(Fox.class, manager);
      try
      {
         DependentContext.INSTANCE.setActive(true);
         Fox fox = manager.getInstance(foxBean);
         assert !Reflections.isProxy(fox);
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }
   
   @Test(groups = "Reflections")
   @SpecAssertion(section = "5.4")
   public void testSimpleWebBeanClientProxyIsSerializable() throws IOException, ClassNotFoundException
   {
      Bean<TunedTuna> tunaBean = BeanFactory.createSimpleBean(TunedTuna.class, manager);
      manager.addBean(tunaBean);
      TunedTuna tuna = manager.getInstance(tunaBean);
      assert Reflections.isProxy(tuna);
      byte[] bytes = serialize(tuna);
      tuna = (TunedTuna) deserialize(bytes);
      assert Reflections.isProxy(tuna);
      assert tuna.getState().equals("tuned");
   }

   @Test(groups = "Reflections", expectedExceptions = UnproxyableDependencyException.class)
   @SpecAssertion(section = "5.4.1")
   public void testInjectionPointWithUnproxyableTypeWhichResolvesToNormalScopedWebBean()
   {
      Bean<FinalTuna> tunaBean = BeanFactory.createSimpleBean(FinalTuna.class, manager);
      manager.addBean(tunaBean);
      @SuppressWarnings("unused")
      FinalTuna tuna = manager.getInstanceByType(FinalTuna.class);      
      assert false;
   }

   @Test(groups = "Reflections")
   @SpecAssertion(section = "5.4.2")
   public void testClientProxyInvocation()
   {
      Bean<TunedTuna> tunaBean = BeanFactory.createSimpleBean(TunedTuna.class, manager);
      manager.addBean(tunaBean);
      TunedTuna tuna = manager.getInstance(tunaBean);
      assert Reflections.isProxy(tuna);
      assert tuna.getState().equals("tuned");
   }
   
   @Test(groups = "Reflections", expectedExceptions=DefinitionException.class)
   public void testGettingUnknownBeanFails() {
      Bean<Tuna> tunaBean = BeanFactory.createSimpleBean(Tuna.class, manager);
      @SuppressWarnings("unused")
      Tuna tuna = manager.getInstance(tunaBean);
      assert false;
   }

}
