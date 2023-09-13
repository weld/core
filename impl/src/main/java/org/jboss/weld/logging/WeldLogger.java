/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.logging;

import static org.jboss.weld.logging.WeldLogger.WELD_PROJECT_CODE;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * A source of localized log/bundle messages and exceptions. Note that this interface extends {@link BasicLogger} so that
 * regular logger methods are available.
 *
 * @author Martin Kouba
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface WeldLogger extends BasicLogger {

    String CATCHING_MARKER = "Catching";

    String WELD_PROJECT_CODE = "WELD-";

    /**
     * Replacement for <code>org.slf4j.ext.XLogger.throwing(Level.DEBUG, e)</code>.
     *
     * @param throwable
     */
    @LogMessage(level = Level.DEBUG)
    @Message(id = 0, value = CATCHING_MARKER)
    void catchingDebug(@Cause Throwable throwable);

}
