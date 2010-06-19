package org.jboss.weld.examples.pastecode.session;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

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
