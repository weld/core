package org.jboss.weld.logging;

import static org.jboss.weld.logging.WeldLogger.WELD_PROJECT_CODE;

import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.exceptions.IllegalArgumentException;

/**
 * Log messages for validation related classes.
 *
 * Message IDs: 002000 - 002099
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface InvokerLogger extends WeldLogger{

    @Message(id = 2000, value = "TBD {0}", format = Message.Format.MESSAGE_FORMAT)
    IllegalArgumentException tbd(Object param1);
}
