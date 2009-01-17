package org.jboss.webbeans.test.unit.event;

import javax.webbeans.RequestScoped;

@AnotherDeploymentType
@RequestScoped
class Tuna
{
   
   public String getName()
   {
      return "Ophir";
   }

}
