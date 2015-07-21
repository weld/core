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

import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.InvalidObjectException;

/**
 * Log messages for events
 *
 * Message ids: 000400 - 000499
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface EventLogger extends WeldLogger {

    EventLogger LOG = Logger.getMessageLogger(EventLogger.class, Category.EVENT.getName());

    @LogMessage(level = Level.DEBUG)
    @Message(id = 400, value = "Sending event {0} directly to observer {1}", format = Format.MESSAGE_FORMAT)
    void asyncFire(Object param1, Object param2);

    @LogMessage(level = Level.ERROR)
    @Message(id = 401, value = "Failure while notifying an observer of event {0}", format = Format.MESSAGE_FORMAT)
    void asyncObserverFailure(Object param1);

    @Message(id = 403, value = "Proxy required")
    InvalidObjectException serializationProxyRequired();

    @Message(id = 404, value = "Conditional observer method cannot be declared by a @Dependent scoped bean: {0}\n\tat {1}\n  StackTrace:", format = Format.MESSAGE_FORMAT)
    DefinitionException invalidScopedConditionalObserver(Object param1, Object stackElement);

    @Message(id = 405, value = "Observer method cannot have more than one event parameter annotated with @Observes or @ObservesAsync: {0}\n\tat {1}\n  StackTrace:", format = Format.MESSAGE_FORMAT)
    DefinitionException multipleEventParameters(Object param1, Object stackElement);

    @Message(id = 406, value = "Observer method cannot have a parameter annotated with @Disposes: {0}\n\tat {1}\n  StackTrace:", format = Format.MESSAGE_FORMAT)
    DefinitionException invalidDisposesParameter(Object param1, Object stackElement);

    @Message(id = 407, value = "Observer method cannot be annotated with @Produces: {0}\n\tat {1}\n  StackTrace:", format = Format.MESSAGE_FORMAT)
    DefinitionException invalidProducer(Object param1, Object stackElement);

    @Message(id = 408, value = "Observer method cannot be annotated with @Inject, observer methods are automatically injection points: {0}\n\tat {1}\n  StackTrace:", format = Format.MESSAGE_FORMAT)
    DefinitionException invalidInitializer(Object param1, Object stackElement);

    @Message(id = 409, value = "Observer method for container lifecycle event can only inject BeanManager: {0}\n\tat {1}\n  StackTrace:", format = Format.MESSAGE_FORMAT)
    DefinitionException invalidInjectionPoint(Object param1, Object stackElement);

    @Message(id = 410, value = "Observer method cannot define @WithAnnotations: {0}\n\tat {1}\n  StackTrace:", format = Format.MESSAGE_FORMAT)
    DefinitionException invalidWithAnnotations(Object param1, Object stackElement);

    @LogMessage(level=Level.INFO)
    @Message(id = 411, value = "Observer method {0} receives events for all annotated types. Consider restricting events using @WithAnnotations or a generic type with bounds.", format = Format.MESSAGE_FORMAT)
    void unrestrictedProcessAnnotatedTypes(Object param1);

    @Message(id = 412, value = "ObserverMethod.{0}() returned null for {1}", format = Format.MESSAGE_FORMAT)
    DefinitionException observerMethodsMethodReturnsNull(Object param1, Object param2);

    @Message(id = 413, value = "{0} cannot be replaced by an observer method with a different bean class {1}", format = Format.MESSAGE_FORMAT)
    DefinitionException beanClassMismatch(ObserverMethod<?> originalObserverMethod, ObserverMethod<?> observerMethod);

}
