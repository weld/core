package org.jboss.weld.environment.servlet.test.context.async;

import java.io.IOException;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

/**
 * @author Tomas Remes
 */
public class SimpleAsyncListener implements AsyncListener {

    public static boolean onCompleteCalled = false;
    public static boolean onTimeoutCalled = false;
    public static boolean onErrorCalled = false;
    public static boolean onStartAsyncCalled = false;

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.AsyncListener#onComplete(javax.servlet.AsyncEvent)
     */
    @Override
    public void onComplete(AsyncEvent event) throws IOException {
        onCompleteCalled = true;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.AsyncListener#onTimeout(javax.servlet.AsyncEvent)
     */
    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        onTimeoutCalled = true;
        event.getAsyncContext().complete();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.AsyncListener#onError(javax.servlet.AsyncEvent)
     */
    @Override
    public void onError(AsyncEvent event) throws IOException {
        onErrorCalled = true;
        event.getAsyncContext().complete();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.AsyncListener#onStartAsync(javax.servlet.AsyncEvent)
     */
    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {
        onStartAsyncCalled = true;
    }

    public static void reset(){
        onCompleteCalled = false;
        onTimeoutCalled = false;
        onErrorCalled = false;
        onStartAsyncCalled = false;
    }

}
