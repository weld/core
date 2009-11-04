package org.jboss.weld.tests.beanDeployment.managed.multiple;

import javax.enterprise.context.RequestScoped;

@RequestScoped
class Tuna
{
   
   public String getName()
   {
      return "Ophir";
   }

}
