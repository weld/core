package org.jboss.weld.tests.injectionPoint;

import java.io.Serializable;
import java.util.Timer;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

public class TimerManager implements Serializable
{

   private static final long serialVersionUID = 5156835887786174326L;

   @Produces
   @RequestScoped
   public Timer getTimer()
   {
      return new Timer();
   }

}
