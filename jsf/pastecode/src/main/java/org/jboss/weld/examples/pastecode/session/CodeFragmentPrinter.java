package org.jboss.weld.examples.pastecode.session;

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.jboss.weld.examples.pastecode.model.CodeFragment;

@Stateless
public class CodeFragmentPrinter
{
   
   private static final int ONE_MINUTE = 60 * 1000;
   
   @Resource
   private TimerService timerService;
   
   @Inject 
   private CodeFragmentLogger logger;
   
   @Inject 
   private Logger log;
   
   public void startTimer()
   {
      timerService.createTimer(ONE_MINUTE, ONE_MINUTE, null);
   }
   
   @Timeout
   public void print()
   {
      if (logger.getLog().size() > 0)
      {
         log.info("These code fragments pasted in the last minute: ");
         for (CodeFragment fragment : logger.getLog())
         {
            log.info(fragment.toString());
         }
         log.info("-----------------------------------------------------");
         logger.clearLog();
      }
      else
      {
         log.info("No fragments pasted in the last minute");
      }
   }

}
