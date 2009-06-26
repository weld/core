package org.jboss.webbeans.test.unit.implementation.enterprise.lifecycle;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.enterprise.inject.Current;

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
