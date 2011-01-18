package org.jboss.weld.examples.pastecode.session;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * Responsible for starting the timer for printing recently added code fragments
 * @author Pete Muir
 *
 */
@Startup @Singleton
public class TimerStartup
{
   
   @EJB // Due to EJBTHREE-2227 this can't be @Inject
   private CodeFragmentPrinter codeFragmentPrinter;
   
   @PostConstruct
   public void startup()
   {
      codeFragmentPrinter.startTimer();
   }

}
