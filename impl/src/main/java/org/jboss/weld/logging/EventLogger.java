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

import java.lang.invoke.MethodHandles;

import jakarta.enterprise.inject.spi.ObserverMethod;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.exceptions.InvalidObjectException;

/**
 * Log messages for events
 *
 * Message ids: 000400 - 000499
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface EventLogger extends WeldLogger {

    EventLogger LOG = Logger.getMessageLogger(MethodHandles.lookup(), EventLogger.class, Category.EVENT.getName());

    @LogMessage(level = Level.DEBUG)
    @Message(id = 400, value = "Sending event {0} directly to observer {1}", format = Format.MESSAGE_FORMAT)
    void asyncFire(Object param1, Object param2);

    @LogMessage(level = Level.ERROR)
    @Message(id = 401, value = "Failure while notifying an observer {0} of event {1}.\n {2}", format = Format.MESSAGE_FORMAT)
    void asyncObserverFailure(Object param1, Object param2, Object param3);

    @Message(id = 403, value = "Proxy required")
    InvalidObjectException serializationProxyRequired();

    @Message(id = 404, value = "Conditional observer method cannot be declared by a @Dependent scoped bean: {0}\n\tat {1}\n  StackTrace:", format = Format.MESSAGE_FORMAT)
    DefinitionException invalidScopedConditionalObserver(Object param1, Object stackElement);

    @SuppressWarnings("weldlog:msg-value")
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

    @LogMessage(level = Level.INFO)
    @Message(id = 411, value = "Observer method {0} receives events for all annotated types. Consider restricting events using @WithAnnotations or a generic type with bounds.", format = Format.MESSAGE_FORMAT)
    void unrestrictedProcessAnnotatedTypes(Object param1);

    @Message(id = 412, value = "ObserverMethod.{0}() returned null for {1}", format = Format.MESSAGE_FORMAT)
    DefinitionException observerMethodsMethodReturnsNull(Object param1, Object param2);

    @SuppressWarnings({ "weldlog:method-sig" })
    @Message(id = 413, value = "{0} cannot be replaced by an observer method with a different bean class {1}", format = Format.MESSAGE_FORMAT)
    DefinitionException beanClassMismatch(ObserverMethod<?> originalObserverMethod, ObserverMethod<?> observerMethod);

    @SuppressWarnings({ "weldlog:method-sig" })
    @Message(id = 414, value = "Observer method for container lifecycle event cannot be asynchronous. {0}\n\tat {1}\n  StackTrace:", format = Format.MESSAGE_FORMAT)
    DefinitionException asyncContainerLifecycleEventObserver(ObserverMethod<?> observer, Object stackElement);

    @Message(id = 415, value = "Custom implementation of observer method does not override either notify(T) or notify(EventContext<T>): {0}", format = Format.MESSAGE_FORMAT)
    DefinitionException notifyMethodNotImplemented(Object observer);

    @Message(id = 416, value = "None or multiple event parameters declared on: {0}\n\tat {1}\n  StackTrace:", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException noneOrMultipleEventParametersDeclared(Object method, Object stackElement);

    @LogMessage(level = Level.WARN)
    @Message(id = 417, value = "The original observed type {0} is not assignable from {1} set by extension {2} - the observer method invocation may result in runtime exception!", format = Format.MESSAGE_FORMAT)
    void originalObservedTypeIsNotAssignableFrom(Object originalObservedType, Object observedType, Object extension);

    @SuppressWarnings({ "weldlog:method-sig" })
    @Message(id = 418, value = "Observer method for container lifecycle event cannot be static. {0}\n\tat {1}\n  StackTrace:", format = Format.MESSAGE_FORMAT)
    DefinitionException staticContainerLifecycleEventObserver(ObserverMethod<?> observer, Object stackElement);

    @Message(id = 419, value = "{0} is not a valid notification mode for asynchronous observers", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException invalidNotificationMode(Object mode);

    @Message(id = 420, value = "Asynchronous observer notification with timeout option requires an implementation of ExecutorServices which provides an instance of ScheduledExecutorServices.", format = Format.MESSAGE_FORMAT)
    UnsupportedOperationException noScheduledExecutorServicesProvided();

    @Message(id = 421, value = "Invalid input value for asynchronous observer notification timeout. Has to be parseable String, java.lang.Long or long. Original exception: {0}", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException invalidInputValueForTimeout(Object nfe);

    @Message(id = 422, value = "WeldEvent.select(Type subtype, Annotation... qualifiers) can be invoked only on an instance of WeldEvent<Object>.", format = Format.MESSAGE_FORMAT)
    IllegalStateException selectByTypeOnlyWorksOnObject();
}
