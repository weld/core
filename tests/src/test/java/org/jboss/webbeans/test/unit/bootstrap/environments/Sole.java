package org.jboss.webbeans.test.unit.bootstrap.environments;

import javax.ejb.EJB;
import javax.enterprise.inject.Named;
import javax.enterprise.inject.deployment.Production;

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
