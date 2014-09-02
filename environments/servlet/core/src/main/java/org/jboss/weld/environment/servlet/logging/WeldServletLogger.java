/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.servlet.logging;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;



/**
 * @author Kirill Gaevskii
 *
 */
public interface WeldServletLogger extends BasicLogger {
    WeldServletLogger LOG = Logger.getMessageLogger(WeldServletLogger.class, Category.WELDSERVLET.getName());

    String CATCHING_MARKER = "Catching";
    String WELD_PROJECT_CODE = "WELD-SERVLET";

    /**
     * Replacement for <code>org.slf4j.ext.XLogger.throwing(Level.DEBUG, e)</code>.
     *
     * @param throwable
     */
    @LogMessage(level = Level.DEBUG)
    @Message(id = 0, value = CATCHING_MARKER)
    void catchingDebug(@Cause Throwable throwable);

    @LogMessage(level = Level.WARN)
    @Message(id = 1, value = "@Resource injection not available in simple beans")
    void resourceInjectionNotAvailable();

    @LogMessage(level = Level.INFO)
    @Message(id = 2, value = "No supported servlet container detected, CDI injection will NOT be available in Servlets, Filters or Listeners")
    void noSupportedServletContainerDetected();

    @LogMessage(level = Level.INFO)
    @Message(id = 3, value = "Container detection skipped - custom container class loaded: {0}.", format = Format.MESSAGE_FORMAT)
    void containerDetectionSkipped(Object param1);

    @LogMessage(level = Level.WARN)
    @Message(id = 4, value = "Unable to instantiate custom container class: {0}.", format = Format.MESSAGE_FORMAT)
    void unableToInstantiateCustomContainerClass(Object param1);
    
    @LogMessage(level = Level.WARN)
    @Message(id = 5, value = "Found both WEB-INF/beans.xml and WEB-INF/classes/META-INF/beans.xml. It''s not portable to use both locations at the same time. Weld is going to use {0}.", format = Format.MESSAGE_FORMAT)
    void foundBothConfiguration(Object param1);
    
    @LogMessage(level = Level.TRACE)
    @Message(id = 6, value = "Exactly one constructor ({0}) annotated with @Inject defined, using it as the bean constructor for {1}", format = Format.MESSAGE_FORMAT)
    void foundOneInjectableConstructor(Object param1, Object param2);

    @LogMessage(level = Level.INFO)
    @Message(id = 7, value = "org.jboss.weld.environment.servlet.EnhancedListener used for ServletContext notifications")
    void enhancedListenerUsedForNotifications();
    
    @LogMessage(level = Level.INFO)
    @Message(id = 8, value = "Initialize Weld using ServletContextListener")
    void initializeWeldUsingServletContextListener();
    
    @LogMessage(level = Level.INFO)
    @Message(id = 9, value = "Initialize Weld using ServletContainerInitializer")
    void initializeWeldUsingServletContainerInitializer();

    @LogMessage(level = Level.INFO)
    @Message(id = 10, value = "org.jboss.weld.environment.servlet.Listener used for ServletRequest and HttpSession notifications")
    void listenerUserForServletRequestAndHttpSessionNotifications();
    
    @LogMessage(level = Level.INFO)
    @Message(id = 11, value = "Problem when iterating through {0}", format = Format.MESSAGE_FORMAT)
    void problemWhenInterating(Object param1, @Cause Throwable cause);
    
    @LogMessage(level = Level.INFO)
    @Message(id = 12, value = "Could not read context {0}: Trying to create it!", format = Format.MESSAGE_FORMAT)
    void couldNotReadContext(Object param1);

    @LogMessage(level = Level.INFO)
    @Message(id = 13, value = "BeanManager reference bound to {0}.")
    void beanManagerReferenceBoundTo(Object param1);

    @LogMessage(level = Level.INFO)
    @Message(id = 14, value = "Successfully unbound BeanManager reference.")
    void successfullyUnboundBeanManagerReference();

    @LogMessage(level = Level.WARN)
    @Message(id = 15, value = "Failed to unbind BeanManager reference!")
    void failedToUnbindBeanManagerReference();

    @LogMessage(level = Level.ERROR)
    @Message(id = 16, value = "Could not create context: {0}.", format = Format.MESSAGE_FORMAT)
    void couldntCreateContext(Object param1);
}
