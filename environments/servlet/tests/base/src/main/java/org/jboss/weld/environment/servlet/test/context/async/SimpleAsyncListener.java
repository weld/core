package org.jboss.weld.environment.servlet.test.context.async;

import java.io.IOException;

import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;

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
     * @see jakarta.servlet.AsyncListener#onComplete(jakarta.servlet.AsyncEvent)
     */
    @Override
    public void onComplete(AsyncEvent event) throws IOException {
        onCompleteCalled = true;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.servlet.AsyncListener#onTimeout(jakarta.servlet.AsyncEvent)
     */
    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        onTimeoutCalled = true;
        event.getAsyncContext().complete();
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.servlet.AsyncListener#onError(jakarta.servlet.AsyncEvent)
     */
    @Override
    public void onError(AsyncEvent event) throws IOException {
        onErrorCalled = true;
        event.getAsyncContext().complete();
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.servlet.AsyncListener#onStartAsync(jakarta.servlet.AsyncEvent)
     */
    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {
        onStartAsyncCalled = true;
    }

    public static void reset() {
        onCompleteCalled = false;
        onTimeoutCalled = false;
        onErrorCalled = false;
        onStartAsyncCalled = false;
    }

}
