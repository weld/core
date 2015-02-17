package org.jboss.weld.examples.pastecode.session;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.weld.examples.pastecode.model.CodeFragment;

@Stateless
public class CodeFragmentPrinter {

    @Inject
    private CodeFragmentLogger logger;

    @Inject
    private Logger log;

    public void print(@Observes TimerEvent event) {
        // Print the code fragments retrieved in the last minute to the log
        if (logger.getLog().size() > 0) {
            log.info("These code fragments pasted in the last minute: ");
            for (CodeFragment fragment : logger.getLog()) {
                log.info(fragment.toString());
            }
            log.info("-----------------------------------------------------");
            logger.clearLog();
        } else {
            log.info("No fragments pasted in the last minute");
        }
    }
}
