package org.jboss.webbeans.test.components.broken;

import javax.webbeans.Produces;
import javax.webbeans.Production;

@Production
public class ComponentWithStaticProducerMethod
{

   @Produces public static String getString()
   {
      return "Pete";
   }
   
}
