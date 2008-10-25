package org.jboss.webbeans.test.beans.broken;

import javax.webbeans.Produces;
import javax.webbeans.Production;

@Production
public class BeanWithStaticProducerMethod
{

   @Produces public static String getString()
   {
      return "Pete";
   }
   
}
