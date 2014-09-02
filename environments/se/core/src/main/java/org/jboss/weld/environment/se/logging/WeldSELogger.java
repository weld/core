package org.jboss.weld.environment.se.logging;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.environment.logging.Category;
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
@MessageLogger(projectCode = WeldEnvironmentLogger.WELD_ENV_PROJECT_CODE)
public interface WeldSELogger extends WeldEnvironmentLogger {
    WeldSELogger LOG = Logger.getMessageLogger(WeldSELogger.class, Category.BOOTSTRAP.getName());
}
