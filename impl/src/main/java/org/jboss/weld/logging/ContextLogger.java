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

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;

import jakarta.enterprise.context.spi.Context;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.exceptions.IllegalStateException;

/**
 * Log messages for Contexts
 *
 * Message Ids: 000200 - 000299
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface ContextLogger extends WeldLogger {

    ContextLogger LOG = Logger.getMessageLogger(MethodHandles.lookup(), ContextLogger.class, Category.CONTEXT.getName());

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

    @Message(id = 211, value = "The delimiter \"{0}\" should not be in the prefix \"{1}\"", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException delimiterInPrefix(Object param1, Object param2);

    @Message(id = 212, value = "No contextual specified to retrieve (null)")
    IllegalArgumentException contextualIsNull();

    @Message(id = 213, value = "No bean store available for {0}", format = Format.MESSAGE_FORMAT)
    IllegalStateException noBeanStoreAvailable(Object param1);

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

    @Message(id = 223, value = "Context.getScope() returned null for {0}", format = Format.MESSAGE_FORMAT)
    DefinitionException contextHasNullScope(Object param1);

    @LogMessage(level = Level.WARN)
    @Message(id = 224, value = "Unable to clear the bean store {0}.", format = Format.MESSAGE_FORMAT)
    void unableToClearBeanStore(Object beanStore);

    /**
     *
     * @param context
     * @param info Some additional info, e.g. HTTP request for HttpSessionContext
     */
    @LogMessage(level = Level.WARN)
    @Message(id = 225, value = "Bean store leak detected during {0} association: {1}", format = Format.MESSAGE_FORMAT)
    void beanStoreLeakDuringAssociation(Object context, Object info);

    @SuppressWarnings({ "weldlog:method-sig" })
    @Message(id = 226, value = "Cannot register additional context for scope: {0}, {1}", format = Format.MESSAGE_FORMAT)
    DeploymentException cannotRegisterContext(Class<? extends Annotation> scope, Context context);

    @Message(id = 227, value = "Bean identifier index inconsistency detected - the distributed container probably does not work with identical applications\nExpected hash: {0}\nCurrent index: {1}", format = Format.MESSAGE_FORMAT)
    IllegalStateException beanIdentifierIndexInconsistencyDetected(Object hash, Object index);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 228, value = "Bean store leak detected during {0} association - instances of beans with the following identifiers might not be destroyed correctly: {1}", format = Format.MESSAGE_FORMAT)
    void beanStoreLeakAffectedBeanIdentifiers(Object context, Object identifiers);

    @Message(id = 229, value = "Contextual reference of {0} is not valid after container {1} shutdown", format = Format.MESSAGE_FORMAT)
    IllegalStateException contextualReferenceNotValidAfterShutdown(Object bean, Object contextId);

}
