package org.jboss.weld.examples.pastecode.session;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 * Responsible for starting the timer for printing recently added code fragments
 * @author Pete Muir
 *
 */
@Startup @Singleton
public class TimerStartup
{
   
   @Inject 
   private CodeFragmentPrinter codeFragmentPrinter;
   
   @PostConstruct
   public void startup()
   {
      codeFragmentPrinter.startTimer();
   }

}
