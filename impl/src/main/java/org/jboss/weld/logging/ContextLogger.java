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

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.exceptions.IllegalArgumentException;

/**
 * Log messages for Contexts
 *
 * Message Ids: 000200 - 000299
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface ContextLogger extends WeldLogger {

    ContextLogger LOG = Logger.getMessageLogger(ContextLogger.class, Category.CONTEXT.getName());

    @LogMessage(level = Level.TRACE)
    @Message(id = 200, value = "Looked for {0} and got {1} in {2}", format = Format.MESSAGE_FORMAT)
    void contextualInstanceFound(Object param1, Object param2, Object param3);

    @LogMessage(level = Level.TRACE)
    @Message(id = 201, value = "Context {0} cleared", format = Format.MESSAGE_FORMAT)
    void contextCleared(Object param1);

    @LogMessage(level = Level.TRACE)
    @Message(id = 202, value = "Added {0} with key {1} to {2}", format = Format.MESSAGE_FORMAT)
    void contextualInstanceAdded(Object param1, Object param2, Object param3);

    @LogMessage(level = Level.TRACE)
    @Message(id = 203, value = "Removed {0} from {1}", format = Format.MESSAGE_FORMAT)
    void contextualInstanceRemoved(Object param1, Object param2);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 204, value = "Restoring session {0}", format = Format.MESSAGE_FORMAT)
    String sessionRestored(Object param1);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 205, value = "Ending session {0}", format = Format.MESSAGE_FORMAT)
    String sessionEnded(Object param1);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 206, value = "Starting request {0}", format = Format.MESSAGE_FORMAT)
    String requestStarted(Object param1);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 207, value = "Ending request {0}", format = Format.MESSAGE_FORMAT)
    String requestEnded(Object param1);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 208, value = "Starting application {0}", format = Format.MESSAGE_FORMAT)
    String applicationStarted(Object param1);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 209, value = "Ending application {0}", format = Format.MESSAGE_FORMAT)
    String applicationEnded(Object param1);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 210, value = "Attached dependent instance {0} to {1}", format = Format.MESSAGE_FORMAT)
    String dependentInstanceAttached(Object param1, Object param2);

    @Message(id = 211, value = "The delimiter \"{0}\" should not be in the prefix \"{1}\"", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException delimiterInPrefix(Object param1, Object param2);

    @Message(id = 212, value = "No contextual specified to retrieve (null)")
    IllegalArgumentException contextualIsNull();

    @Message(id = 213, value = "No bean store available for {0}", format = Format.MESSAGE_FORMAT)
    IllegalStateException noBeanStoreAvailable(Object param1);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 214, value = "Restoring conversation {0}", format = Format.MESSAGE_FORMAT)
    String conversationRestored(Object param1);

    @LogMessage(level = Level.TRACE)
    @Message(id = 215, value = "Bean store {0} is detached", format = Format.MESSAGE_FORMAT)
    void beanStoreDetached(Object param1);

    @LogMessage(level = Level.TRACE)
    @Message(id = 216, value = "Updating underlying store with contextual {0} under ID {1}", format = Format.MESSAGE_FORMAT)
    void updatingStoreWithContextualUnderId(Object param1, Object param2);

    @LogMessage(level = Level.TRACE)
    @Message(id = 217, value = "Adding detached contextual {0} under ID {1}", format = Format.MESSAGE_FORMAT)
    void addingDetachedContextualUnderId(Object param1, Object param2);

    @LogMessage(level = Level.TRACE)
    @Message(id = 218, value = "Removed {0} from session {1}", format = Format.MESSAGE_FORMAT)
    void removedKeyFromSession(Object param1, Object param2);

    @LogMessage(level = Level.TRACE)
    @Message(id = 219, value = "Unable to remove {0} from non-existent session", format = Format.MESSAGE_FORMAT)
    void unableToRemoveKeyFromSession(Object param1);

    @LogMessage(level = Level.TRACE)
    @Message(id = 220, value = "Added {0} to session {1}", format = Format.MESSAGE_FORMAT)
    void addedKeyToSession(Object param1, Object param2);

    @LogMessage(level = Level.TRACE)
    @Message(id = 221, value = "Unable to add {0} to session as no session could be obtained", format = Format.MESSAGE_FORMAT)
    void unableToAddKeyToSession(Object param1);

    @LogMessage(level = Level.TRACE)
    @Message(id = 222, value = "Loading bean store {0} map from session {1}", format = Format.MESSAGE_FORMAT)
    void loadingBeanStoreMapFromSession(Object param1, Object param2);

}