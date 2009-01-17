package org.jboss.webbeans.test.unit.lookup;

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
