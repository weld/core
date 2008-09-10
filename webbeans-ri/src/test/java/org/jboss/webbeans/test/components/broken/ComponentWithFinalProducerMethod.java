package org.jboss.webbeans.test.components.broken;

import javax.webbeans.Produces;
import javax.webbeans.Production;
import javax.webbeans.RequestScoped;

@Production
public class ComponentWithFinalProducerMethod
{

   @Produces @RequestScoped public final String getString()
   {
      return "Pete";
   }
   
}
