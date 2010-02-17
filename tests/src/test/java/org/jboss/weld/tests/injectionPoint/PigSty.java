package org.jboss.weld.tests.injectionPoint;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

public class PigSty
{
   @Inject @Special Instance<Pig> pig;
   
   public Pig getPig()
   {
      return pig.select(ExtraSpecial.INSTANCE).get();
   }
   
}

