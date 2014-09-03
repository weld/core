package org.jboss.weld.environment.se.logging;


import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;

/**
 * A source of localized log/bundle messages and exceptions. Note that this interface extends {@link BasicLogger} so that regular logger methods are available.
 *
 * @author Matej Briškár
 * @author Martin Kouba
 * @author Kirill Gaevskii
 */
@MessageLogger(projectCode = "WELD-SE-")
public interface WeldSELogger extends BasicLogger {

    WeldSELogger LOG = Logger.getMessageLogger(WeldSELogger.class, "WELD-SE");

    String CATCHING_MARKER = "Catching";
    String WELD_PROJECT_CODE = "WELD-SE-";

    /**
     * Replacement for <code>org.slf4j.ext.XLogger.throwing(Level.DEBUG, e)</code>.
     *
     * @param throwable
     */
    @LogMessage(level = Level.DEBUG)
    @Message(id = 1, value = CATCHING_MARKER)
    void catchingDebug(@Cause Throwable throwable);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 2, value = "System property org.jboss.weld.se.archive.isolation is set to {0}.", format = Format.MESSAGE_FORMAT)
    void multipleIsolation(Object param1);

}
