package org.jboss.weld.tests.beanDeployment.managed.single;

import javax.enterprise.context.RequestScoped;

@RequestScoped
class Tuna
{
   
   public String getName()
   {
      return "Ophir";
   }

}
