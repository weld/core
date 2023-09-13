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
import org.jboss.weld.exceptions.CreationException;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.IllegalStateException;

/**
 * Log messages for interceptors.
 *
 * Message IDs: 001700 - 001799
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface InterceptorLogger extends WeldLogger {

    InterceptorLogger LOG = Logger.getMessageLogger(InterceptorLogger.class, Category.INTERCEPTOR.getName());

    @LogMessage(level = Level.WARN)
    @Message(id = 1700, value = "Interceptor annotation class {0} not found, interception based on it is not enabled", format = Format.MESSAGE_FORMAT)
    void interceptorAnnotationClassNotFound(Object param1);

    @LogMessage(level = Level.TRACE)
    @Message(id = 1701, value = "Invoking next interceptor in chain: {0}", format = Format.MESSAGE_FORMAT)
    void invokingNextInterceptorInChain(Object param1);

    @Message(id = 1702, value = "Interceptor.getInterceptorBindings() returned null for {0}", format = Format.MESSAGE_FORMAT)
    DefinitionException nullInterceptorBindings(Object param1);

    @LogMessage(level = Level.INFO)
    @Message(id = 1703, value = "Unable to determine the @Intercepted Bean<?> for {0}", format = Format.MESSAGE_FORMAT)
    void unableToDetermineInterceptedBean(Object injectionPoint);

    @Message(id = 1704, value = "@Intercepted Bean<?> can only be injected into an interceptor: {0}", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException interceptedBeanCanOnlyBeInjectedIntoInterceptor(Object injectionPoint);

    @Message(id = 1705, value = "Target instance not created - one of the interceptor methods in the AroundConstruct chain did not invoke InvocationContext.proceed() for: {0}", format = Format.MESSAGE_FORMAT)
    CreationException targetInstanceNotCreated(Object constructor);

    @Message(id = 1706, value = "InterceptionFactory.createInterceptedInstance() may only be called once", format = Format.MESSAGE_FORMAT)
    IllegalStateException interceptionFactoryNotReusable();

    @LogMessage(level = Level.DEBUG)
    @Message(id = 1707, value = "InterceptionFactory.configure() was invoked for AnnotatedType: {0}", format = Format.MESSAGE_FORMAT)
    void interceptionFactoryConfigureInvoked(Object param1);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 1708, value = "InterceptionFactory.ignoreFinalMethods() was invoked for AnnotatedType: {0}. Final methods will be ignored during proxy generation!", format = Format.MESSAGE_FORMAT)
    void interceptionFactoryIgnoreFinalMethodsInvoked(Object param1);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 1709, value = "InterceptionFactory skipped wrapper creation for AnnotatedType {0} because no @AroundInvoke interceptor was bound to it.", format = Format.MESSAGE_FORMAT)
    void interceptionFactoryNotRequired(Object param1);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 1710, value = "InterceptionFactory skipped wrapper creation for an internal container construct of type {0}", format = Format.MESSAGE_FORMAT)
    void interceptionFactoryInternalContainerConstruct(Object type);

    // not used anymore, we support this now, see WELD-2533 and WELD-2550
    @Message(id = 1711, value = "InterceptionFactory is not supported on interfaces. Check InterceptionFactory<{0}>", format = Format.MESSAGE_FORMAT)
    IllegalStateException interceptionFactoryNotOnInstance(Object param1);

}