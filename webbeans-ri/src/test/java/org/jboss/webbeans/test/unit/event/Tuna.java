package org.jboss.webbeans.test.unit.event;

import javax.webbeans.RequestScoped;

import org.jboss.webbeans.test.annotations.AnotherDeploymentType;

@AnotherDeploymentType
@RequestScoped
class Tuna
{
   
   public String getName()
   {
      return "Ophir";
   }

}
