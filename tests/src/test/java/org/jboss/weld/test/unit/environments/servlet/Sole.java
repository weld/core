package org.jboss.weld.test.unit.environments.servlet;

import javax.ejb.EJB;
import javax.inject.Named;

@Whitefish
@Named("whitefish")
class Sole implements ScottishFish
{
   
   @EJB HoundLocal hound;
   
   public void ping()
   {
      
   }

}
