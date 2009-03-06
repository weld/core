package org.jboss.webbeans.test.unit.event;

import javax.context.RequestScoped;

@AnotherDeploymentType
@RequestScoped
class Tuna
{
   
   public String getName()
   {
      return "Ophir";
   }

}
