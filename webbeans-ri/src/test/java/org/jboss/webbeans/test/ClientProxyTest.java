package org.jboss.webbeans.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.webbeans.UnproxyableDependencyException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.test.beans.FinalTuna;
import org.jboss.webbeans.test.beans.Fox;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.test.beans.TunedTuna;
import org.jboss.webbeans.util.BeanFactory;
import org.jboss.webbeans.util.Reflections;
import org.testng.annotations.Test;

@SpecVersion("PDR")
public class ClientProxyTest extends AbstractTest
{

   @Test(groups = "Reflections")
   @SpecAssertion(section = { "4.4", "4.8" })
   public void testReflectionsUsedForNormalScope()
   {
      Bean<Tuna> tunaBean = BeanFactory.createSimpleBean(Tuna.class, manager);
      Tuna tuna = manager.getInstance(tunaBean);
      assert Reflections.isProxy(tuna);
   }

   @Test(groups = "Reflections")
   @SpecAssertion(section = { "4.4", "4.8" })
   public void testReflectionsNotUsedForPseudoScope()
   {
      Bean<Fox> foxBean = BeanFactory.createSimpleBean(Fox.class, manager);
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
   @SpecAssertion(section = "4.4")
   public void testSimpleWebBeanReflectionsIsSerializable() throws IOException, ClassNotFoundException
   {
      Bean<TunedTuna> tunaBean = BeanFactory.createSimpleBean(TunedTuna.class, manager);
      TunedTuna tuna = manager.getInstance(tunaBean);
      assert Reflections.isProxy(tuna);
      byte[] bytes = serializeBean(tuna);
      tuna = (TunedTuna) deserializeBean(bytes);
      assert Reflections.isProxy(tuna);
      assert tuna.getState().equals("tuned");
   }

   @Test(groups = "Reflections", expectedExceptions = UnproxyableDependencyException.class)
   @SpecAssertion(section = "4.4.1")
   public void testInjectionPointWithUnproxyableTypeWhichResolvesToNormalScopedWebBean()
   {
      Bean<FinalTuna> tunaBean = BeanFactory.createSimpleBean(FinalTuna.class, manager);
      manager.addBean(tunaBean);
      FinalTuna tuna = manager.getInstanceByType(FinalTuna.class);      
      assert false;
   }

   @Test(groups = "Reflections")
   @SpecAssertion(section = "4.4.2")
   public void testReflectionsInvocation()
   {
      Bean<TunedTuna> tunaBean = BeanFactory.createSimpleBean(TunedTuna.class, manager);
      manager.addBean(tunaBean);
      TunedTuna tuna = manager.getInstance(tunaBean);
      assert Reflections.isProxy(tuna);
      assert tuna.getState().equals("tuned");
   }
   
   @Test(groups = "Reflections")
   public void testProxyCreationDoesImplicitAddBean() {
      Bean<Tuna> tunaBean = BeanFactory.createSimpleBean(Tuna.class, manager);
      Tuna tuna = manager.getInstance(tunaBean);
      assert manager.getBeans().size() == 2;
   }

}
