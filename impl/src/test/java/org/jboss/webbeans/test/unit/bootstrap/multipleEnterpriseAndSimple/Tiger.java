package org.jboss.webbeans.test.unit.bootstrap.multipleEnterpriseAndSimple;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.inject.Production;

@Production
@Stateful
@Synchronous
public class Tiger implements TigerLocal
{
   
   @Remove
   public void remove()
   {
      
   }

}
