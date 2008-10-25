package org.jboss.webbeans.test.beans.broken;

import javax.webbeans.Produces;
import javax.webbeans.Production;
import javax.webbeans.RequestScoped;

@Production
public class BeanWithFinalProducerMethod
{

   @Produces @RequestScoped public final String getString()
   {
      return "Pete";
   }
   
}
