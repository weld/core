package org.jboss.webbeans.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.webbeans.DefinitionException;
import javax.webbeans.UnproxyableDependencyException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.test.beans.Fox;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.test.beans.TunedTuna;
import org.jboss.webbeans.test.beans.broken.FinalTuna;
import org.jboss.webbeans.util.BeanFactory;
import org.jboss.webbeans.util.Reflections;
import org.testng.annotations.Test;

@SpecVersion("20081206")
public class ClientProxyTest extends AbstractTest
{

   @Test(groups = "Reflections")
   @SpecAssertion(section = { "5.4" })
   public void testClientProxyUsedForNormalScope()
   {
      Bean<Tuna> tunaBean = BeanFactory.createSimpleBean(Tuna.class);
      manager.addBean(tunaBean);
      Tuna tuna = manager.getInstance(tunaBean);
      assert Reflections.isProxy(tuna);
   }

   @Test(groups = "Reflections")
   @SpecAssertion(section = { "5.4" })
   public void testClientProxyNotUsedForPseudoScope()
   {
      Bean<Fox> foxBean = BeanFactory.createSimpleBean(Fox.class);
      Fox fox = manager.getInstance(foxBean);
      assert !Reflections.isProxy(fox);
   }

   private byte[] serializeBean(Object instance) throws IOException {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bytes);
      out.writeObject(instance);
      return bytes.toByteArray();
   }
   
   private Object deserializeBean(byte[] bytes) throws IOException, ClassNotFoundException {
      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
      return in.readObject();
   }
   
   @Test(groups = "Reflections")
   @SpecAssertion(section = "5.4")
   public void testSimpleWebBeanClientProxyIsSerializable() throws IOException, ClassNotFoundException
   {
      Bean<TunedTuna> tunaBean = BeanFactory.createSimpleBean(TunedTuna.class);
      manager.addBean(tunaBean);
      TunedTuna tuna = manager.getInstance(tunaBean);
      assert Reflections.isProxy(tuna);
      byte[] bytes = serializeBean(tuna);
      tuna = (TunedTuna) deserializeBean(bytes);
      assert Reflections.isProxy(tuna);
      assert tuna.getState().equals("tuned");
   }

   @Test(groups = "Reflections", expectedExceptions = UnproxyableDependencyException.class)
   @SpecAssertion(section = "5.4.1")
   public void testInjectionPointWithUnproxyableTypeWhichResolvesToNormalScopedWebBean()
   {
      Bean<FinalTuna> tunaBean = BeanFactory.createSimpleBean(FinalTuna.class);
      manager.addBean(tunaBean);
      FinalTuna tuna = manager.getInstanceByType(FinalTuna.class);      
      assert false;
   }

   @Test(groups = "Reflections")
   @SpecAssertion(section = "5.4.2")
   public void testClientProxyInvocation()
   {
      Bean<TunedTuna> tunaBean = BeanFactory.createSimpleBean(TunedTuna.class);
      manager.addBean(tunaBean);
      TunedTuna tuna = manager.getInstance(tunaBean);
      assert Reflections.isProxy(tuna);
      assert tuna.getState().equals("tuned");
   }
   
   @Test(groups = "Reflections", expectedExceptions=DefinitionException.class)
   public void testGettingUnknownBeanFails() {
      Bean<Tuna> tunaBean = BeanFactory.createSimpleBean(Tuna.class);
      Tuna tuna = manager.getInstance(tunaBean);
      assert false;
   }

}
