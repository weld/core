package org.jboss.webbeans.test.unit.bootstrap.environments;

import javax.annotation.Named;
import javax.ejb.EJB;
import javax.inject.Production;

@Production
@Whitefish
@Named("whitefish")
class Sole implements ScottishFish
{
   
   @EJB HoundLocal hound;
   
   public void ping()
   {
      
   }

}
