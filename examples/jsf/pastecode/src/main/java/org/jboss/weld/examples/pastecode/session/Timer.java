package org.jboss.weld.examples.pastecode.session;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerService;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Responsible for starting the timer for printing recently added code fragments
 *
 * @author Pete Muir
 * @author Jozef Hartinger
 */
@Startup
@Singleton
public class Timer {

    private static final int INTERVAL = 30 * 1000;

    @Resource
    private TimerService timerService;

    @Inject
    private Event<TimerEvent> event;

    @PostConstruct
    void startTimer() {
        timerService.createTimer(0, INTERVAL, null);
    }

    @Timeout
    void timeout() {
        event.fire(new TimerEvent());
    }
}
