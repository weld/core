package org.jboss.webbeans.test;

import org.jboss.webbeans.ejb.EjbMetaData;
import org.jboss.webbeans.test.beans.Leopard;
import org.jboss.webbeans.test.beans.Lion;
import org.jboss.webbeans.test.beans.Tiger;
import org.testng.annotations.Test;

public class EjbMetaDataTest
{

   @Test
   public void testStateless()
   {
      EjbMetaData<Lion> lion = new EjbMetaData<Lion>(Lion.class);
      assert lion.isStateless();
      assert lion.getRemoveMethods().isEmpty();
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
      assert leopard.getRemoveMethods().isEmpty();
   }
   
}
