package org.jboss.weld.test.enterprise.lifecycle;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.inject.Inject;

@Stateful
public class Marburg implements UniStadt
{
   @Inject
   private Schloss theCastle;

   @Remove
   public void removeBean()
   {
   }

}
