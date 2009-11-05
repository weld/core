package org.jboss.weld.tests.extensions;

import javax.enterprise.inject.Produces;

public class Stable
{
   
   @Produces @Special Rat rat = new Rat();

   @Produces @Special Horse produce()
   {
      return new Horse();
   }
   
}
