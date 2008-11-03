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
import org.jboss.webbeans.test.util.Util;
import org.testng.annotations.Test;

@SpecVersion("PDR")
public class ClientProxyTest extends AbstractTest
{

   @Test(groups = "clientProxy")
   @SpecAssertion(section = { "4.4", "4.8" })
   public void testClientProxyUsedForNormalScope()
   {
      Bean<Tuna> tunaBean = Util.createSimpleWebBean(Tuna.class, manager);
      Tuna tuna = manager.getInstance(tunaBean);
      assert tuna.getClass().getName().indexOf("$$_javassist_") > 0;
   }

   @Test(groups = "clientProxy")
   @SpecAssertion(section = { "4.4", "4.8" })
   public void testClientProxyNotUsedForPseudoScope()
   {
      Bean<Fox> foxBean = Util.createSimpleWebBean(Fox.class, manager);
      Fox fox = manager.getInstance(foxBean);
      assert fox.getClass() == Fox.class;
   }

   @Test(groups = "clientProxy")
   @SpecAssertion(section = "4.4")
   public void testSimpleWebBeanClientProxyIsSerializable()
   {
      Bean<TunedTuna> tunaBean = Util.createSimpleWebBean(TunedTuna.class, manager);
      manager.addBean(tunaBean);
      TunedTuna tuna = manager.getInstance(tunaBean);
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      ObjectOutputStream out = null;
      ObjectInputStream in = null;
      try
      {
         out = new ObjectOutputStream(bytes);
         out.writeObject(tuna);
         out.flush();
         byte[] data = bytes.toByteArray();
         in = new ObjectInputStream(new ByteArrayInputStream(data));
         tuna = (TunedTuna) in.readObject();
         assert tuna.getState().equals("tuned");
      }
      catch (Exception e)
      {
         e.printStackTrace();
         assert false;
      }
      finally
      {
         try
         {
            if (bytes != null)
            {
               bytes.close();
            }
            if (out != null)
            {
               out.close();
            }
            if (in != null)
            {
               in.close();
            }
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
      assert true;
   }

   @Test(groups = "clientProxy", expectedExceptions = UnproxyableDependencyException.class)
   @SpecAssertion(section = "4.4.1")
   public void testInjectionPointWithUnproxyableTypeWhichResolvesToNormalScopedWebBean()
   {
      Bean<FinalTuna> tunaBean = Util.createSimpleWebBean(FinalTuna.class, manager);
      manager.addBean(tunaBean);
      FinalTuna tuna = manager.getInstanceByType(FinalTuna.class);      
      assert false;
   }

   @Test(groups = "clientProxy")
   @SpecAssertion(section = "4.4.2")
   public void testClientProxyInvocation()
   {
      Bean<TunedTuna> tunaBean = Util.createSimpleWebBean(TunedTuna.class, manager);
      manager.addBean(tunaBean);
      TunedTuna tuna = manager.getInstance(tunaBean);
      assert tuna.getClass().getName().indexOf("$$_javassist_") > 0;
      assert tuna.getState().equals("tuned");
   }
   
   @Test(groups = "clientProxy")
   public void testProxyCreationDoesImplicitAddBean() {
      Bean<Tuna> tunaBean = Util.createSimpleWebBean(Tuna.class, manager);
      Tuna tuna = manager.getInstance(tunaBean);
      assert manager.getBeans().size() == 1;
   }

}
