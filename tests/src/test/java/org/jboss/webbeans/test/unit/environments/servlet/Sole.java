package org.jboss.webbeans.test.unit.environments.servlet;

import javax.ejb.EJB;
import javax.enterprise.inject.Named;

@Whitefish
@Named("whitefish")
class Sole implements ScottishFish
{
   
   @EJB HoundLocal hound;
   
   public void ping()
   {
      
   }

}
