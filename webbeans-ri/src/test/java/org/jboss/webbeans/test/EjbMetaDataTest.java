package org.jboss.webbeans.test;

import org.jboss.webbeans.ejb.EjbMetaData;
import org.jboss.webbeans.test.components.Leopard;
import org.jboss.webbeans.test.components.Lion;
import org.jboss.webbeans.test.components.Tiger;
import org.testng.annotations.Test;

public class EjbMetaDataTest
{

   @Test
   public void testStateless()
   {
      EjbMetaData<Lion> lion = new EjbMetaData<Lion>(Lion.class);
      assert lion.isStateless();
      assert lion.getRemoveMethods() == null;
   }
   
   @Test
   public void testStateful()
   {
      EjbMetaData<Tiger> tiger = new EjbMetaData<Tiger>(Tiger.class);
      assert tiger.isStateful();
      assert tiger.getRemoveMethods().size() == 1;
   }
   
   @Test
   public void testMessageDriven()
   {
      EjbMetaData<Leopard> leopard = new EjbMetaData<Leopard>(Leopard.class);
      assert leopard.isMessageDriven();
      assert leopard.getRemoveMethods() == null;
   }
   
}
