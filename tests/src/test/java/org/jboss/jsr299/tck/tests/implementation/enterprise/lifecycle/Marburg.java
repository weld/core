package org.jboss.jsr299.tck.tests.implementation.enterprise.lifecycle;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.inject.Current;

@Stateful
public class Marburg implements UniStadt
{
   @Current
   private Schloss theCastle;

   @Remove
   public void removeBean()
   {
   }

}
