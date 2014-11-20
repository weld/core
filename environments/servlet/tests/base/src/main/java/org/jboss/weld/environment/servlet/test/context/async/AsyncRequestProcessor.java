package org.jboss.weld.environment.servlet.test.context.async;

import javax.servlet.AsyncContext;

import org.jboss.weld.test.util.Timer;

/**
 * @author Martin Kouba
 */
public class AsyncRequestProcessor implements Runnable {

    private final AsyncContext actx;

    private final long timerValue;

    private final String dispatchPath;

    private final boolean useDispatch;

    public AsyncRequestProcessor(AsyncContext ctx, long timerValue, boolean useDispatch, String dispatchPath) {
        super();
        this.actx = ctx;
        this.timerValue = timerValue;
        this.dispatchPath = dispatchPath;
        this.useDispatch = useDispatch;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            if (timerValue > 0) {
                // Simulate long running operation
                Timer.startNew(timerValue);
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted");
        }

        // Dispatch or complete
        if (useDispatch) {
            if (dispatchPath != null) {
                actx.dispatch(dispatchPath);
            } else {
                actx.dispatch();
            }
        } else {
            actx.complete();
        }
    }

}
