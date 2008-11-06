package org.jboss.webbeans.test.beans.broken;

import javax.webbeans.Produces;

public class FarmHouseProducer
{
   
   @Produces public Integer getNumberOfBedrooms()
   {
      return null;
   }
   
}
