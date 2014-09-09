package org.jboss.weld.environment.se.logging;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.environment.logging.WeldEnvironmentLogger;

/**
 * A source of localized log/bundle messages and exceptions. Note that this interface extends {@link WeldEnvironmentLogger} so that regular logger methods are available.
 *
 * Message IDs: 002000 - 002099
 *
 * @author Matej Briškár
 * @author Martin Kouba
 * @author Kirill Gaevskii
 */
@MessageLogger(projectCode = "WELD-ENV-")
public interface WeldSELogger extends WeldEnvironmentLogger {
    WeldSELogger LOG = Logger.getMessageLogger(WeldSELogger.class, "WELD-SE");

    @LogMessage(level = Level.DEBUG)
    @Message(id = 2001, value = "System property org.jboss.weld.se.archive.isolation is set to {0}.", format = Format.MESSAGE_FORMAT)
    void multipleIsolation(Object param1);

}
