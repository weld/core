package org.jboss.webbeans.test.unit.bootstrap.multipleEnterpriseAndSimple;

import javax.enterprise.context.RequestScoped;

@RequestScoped
class Tuna
{
   
   public String getName()
   {
      return "Ophir";
   }

}
