package org.jboss.weld.environment.logging;


import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;

/**
 *
 * @author Matej Briškár
 * @author Martin Kouba
 */
public interface WeldEnvironmentLogger extends BasicLogger {

    String CATCHING_MARKER = "Catching";

    String WELD_ENV_PROJECT_CODE = "WELD-ENV-";

    /**
     * Replacement for <code>org.slf4j.ext.XLogger.throwing(Level.DEBUG, e)</code>.
     *
     * @param throwable
     */
    @LogMessage(level = Level.DEBUG)
    @Message(id = 0, value = CATCHING_MARKER)
    void catchingDebug(@Cause Throwable throwable);

    /**
     * Replacement for <code>org.slf4j.ext.XLogger.throwing(Level.TRACE, e)</code>.
     *
     * @param throwable
     */
    @LogMessage(level = Level.TRACE)
    @Message(id = 0, value = CATCHING_MARKER)
    void catchingTrace(@Cause Throwable throwable);


}
