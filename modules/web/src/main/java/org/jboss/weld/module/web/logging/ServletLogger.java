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
package org.jboss.weld.module.web.logging;

import static org.jboss.weld.logging.WeldLogger.WELD_PROJECT_CODE;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.logging.Category;
import org.jboss.weld.logging.WeldLogger;
import org.jboss.weld.module.web.servlet.ServletContextService;

/**
 * Error messages relating to Servlet integration
 *
 * Message ids: 000700 - 000799
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface ServletLogger extends WeldLogger {

    ServletLogger LOG = Logger.getMessageLogger(ServletLogger.class, Category.SERVLET.getName());

    @SuppressWarnings("weldlog:method-interface")
    @Message(id = 707, value = "Non Http-Servlet lifecycle not defined")
    IllegalStateException onlyHttpServletLifecycleDefined();

    @SuppressWarnings("weldlog:method-interface")
    @LogMessage(level = Level.TRACE)
    @Message(id = 708, value = "Initializing request {0}", format = Format.MESSAGE_FORMAT)
    void requestInitialized(Object param1);

    @SuppressWarnings("weldlog:method-interface")
    @LogMessage(level = Level.TRACE)
    @Message(id = 709, value = "Destroying request {0}", format = Format.MESSAGE_FORMAT)
    void requestDestroyed(Object param1);

    @SuppressWarnings("weldlog:method-interface")
    @Message(id = 710, value = "Cannot inject {0} outside of a Servlet request", format = Format.MESSAGE_FORMAT)
    IllegalStateException cannotInjectObjectOutsideOfServletRequest(Object param1, @Cause Throwable cause);

    @SuppressWarnings("weldlog:method-interface")
    @LogMessage(level = Level.WARN)
    @Message(id = 711, value = "Context activation pattern {0} ignored as it is overriden by the integrator.", format = Format.MESSAGE_FORMAT)
    void webXmlMappingPatternIgnored(String pattern);

    @SuppressWarnings("weldlog:method-interface")
    @LogMessage(level = Level.WARN)
    @Message(id = 712, value = "Unable to dissociate context {0} from the storage {1}", format = Format.MESSAGE_FORMAT)
    void unableToDissociateContext(Object context, Object storage);

    @SuppressWarnings({ "weldlog:method-interface", "weldlog:method-sig" })
    @Message(id = 713, value = "Unable to inject ServletContext. None is associated with {0}, {1}", format = Format.MESSAGE_FORMAT)
    IllegalStateException cannotInjectServletContext(ClassLoader classLoader, ServletContextService service);

    @SuppressWarnings("weldlog:method-interface")
    @LogMessage(level = Level.WARN)
    @Message(id = 714, value = "HttpContextLifecycle guard leak detected. The Servlet container is not fully compliant. The value was {0}", format = Format.MESSAGE_FORMAT)
    void guardLeak(int value);

    @SuppressWarnings("weldlog:method-interface")
    @LogMessage(level = Level.WARN)
    @Message(id = 715, value = "HttpContextLifecycle guard not set. The Servlet container is not fully compliant.", format = Format.MESSAGE_FORMAT)
    void guardNotSet();

    @SuppressWarnings("weldlog:method-interface")
    @LogMessage(level = Level.INFO)
    @Message(id = 716, value = "Running in Servlet 2.x environment. Asynchronous request support is disabled.")
    void servlet2Environment();

    @SuppressWarnings("weldlog:method-interface")
    @LogMessage(level = Level.WARN)
    @Message(id = 717, value = "Unable to deactivate context {0} when destroying request {1}", format = Format.MESSAGE_FORMAT)
    void unableToDeactivateContext(Object context, Object request);

    @SuppressWarnings("weldlog:method-interface")
    @LogMessage(level = Level.WARN)
    @Message(id = 718, value = "No EEModuleDescriptor defined for bean archive with ID: {0}. @Initialized and @Destroyed events for ApplicationScoped may be fired twice.", format = Format.MESSAGE_FORMAT)
    void noEeModuleDescriptor(Object beanArchiveId);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 719, value = "An active HttpSessionDestructionContext detected. This is likely a leftover from previous sessions that ended exceptionally. This context will be terminated.", format = Format.MESSAGE_FORMAT)
    void destructionContextLeak();

}