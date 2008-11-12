package org.jboss.webbeans.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.webbeans.UnproxyableDependencyException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.bean.proxy.ClientProxy;
import org.jboss.webbeans.test.beans.FinalTuna;
import org.jboss.webbeans.test.beans.Fox;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.test.beans.TunedTuna;
import org.jboss.webbeans.test.util.Util;
import org.testng.annotations.Test;

@SpecVersion("PDR")
public class ClientProxyTest extends AbstractTest
{

   @Test(groups = "clientProxy")
   @SpecAssertion(section = { "4.4", "4.8" })
   public void testClientProxyUsedForNormalScope()
   {
      Bean<Tuna> tunaBean = Util.createSimpleBean(Tuna.class, manager);
      Tuna tuna = manager.getInstance(tunaBean);
      assert ClientProxy.isProxy(tuna);
   }

   @Test(groups = "clientProxy")
   @SpecAssertion(section = { "4.4", "4.8" })
   public void testClientProxyNotUsedForPseudoScope()
   {
      Bean<Fox> foxBean = Util.createSimpleBean(Fox.class, manager);
      Fox fox = manager.getInstance(foxBean);
      assert !ClientProxy.isProxy(fox);
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
   
   @Test(groups = "clientProxy")
   @SpecAssertion(section = "4.4")
   public void testSimpleWebBeanClientProxyIsSerializable() throws IOException, ClassNotFoundException
   {
      Bean<TunedTuna> tunaBean = Util.createSimpleBean(TunedTuna.class, manager);
      TunedTuna tuna = manager.getInstance(tunaBean);
      assert ClientProxy.isProxy(tuna);
      byte[] bytes = serializeBean(tuna);
      tuna = (TunedTuna) deserializeBean(bytes);
      assert ClientProxy.isProxy(tuna);
      assert tuna.getState().equals("tuned");
   }

   @Test(groups = "clientProxy", expectedExceptions = UnproxyableDependencyException.class)
   @SpecAssertion(section = "4.4.1")
   public void testInjectionPointWithUnproxyableTypeWhichResolvesToNormalScopedWebBean()
   {
      Bean<FinalTuna> tunaBean = Util.createSimpleBean(FinalTuna.class, manager);
      manager.addBean(tunaBean);
      FinalTuna tuna = manager.getInstanceByType(FinalTuna.class);      
      assert false;
   }

   @Test(groups = "clientProxy")
   @SpecAssertion(section = "4.4.2")
   public void testClientProxyInvocation()
   {
      Bean<TunedTuna> tunaBean = Util.createSimpleBean(TunedTuna.class, manager);
      manager.addBean(tunaBean);
      TunedTuna tuna = manager.getInstance(tunaBean);
      assert ClientProxy.isProxy(tuna);
      assert tuna.getState().equals("tuned");
   }
   
   @Test(groups = "clientProxy")
   public void testProxyCreationDoesImplicitAddBean() {
      Bean<Tuna> tunaBean = Util.createSimpleBean(Tuna.class, manager);
      Tuna tuna = manager.getInstance(tunaBean);
      assert manager.getBeans().size() == 2;
   }

}
