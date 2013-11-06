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

import javax.enterprise.context.spi.Context;
import javax.servlet.http.HttpServletRequest;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.exceptions.IllegalStateException;

/**
 * Error messages relating to Servlet integration
 *
 * Message ids: 000700 - 000799
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface ServletLogger extends WeldLogger {

    ServletLogger LOG = Logger.getMessageLogger(ServletLogger.class, Category.SERVLET.getName());

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 700, value = "Not starting Weld/Servlet integration as Weld failed to initialize")
    String notStarting();

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 701, value = "ServletContext is null")
    String contextNull();

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 702, value = "Unable to find BeanManager for {0}", format = Format.MESSAGE_FORMAT)
    String beanManagerNotFound(Object param1);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 703, value = "Cannot obtain request scoped beans from the request")
    String requestScopeBeanStoreMissing();

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 704, value = "Unable to locate bean deployment archive for {0}", format = Format.MESSAGE_FORMAT)
    String beanDeploymentArchiveMissing(Object param1);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 705, value = "Unable to locate bean manager for {0} in {1}", format = Format.MESSAGE_FORMAT)
    String beanManagerForArchiveNotFound(Object param1, Object param2);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 706, value = "Cannot use WeldListener without ServletServices")
    String illegalUseOfWeldListener();

    @Message(id = 707, value = "Non Http-Servlet lifecycle not defined")
    IllegalStateException onlyHttpServletLifecycleDefined();

    @LogMessage(level = Level.TRACE)
    @Message(id = 708, value = "Initializing request {0}", format = Format.MESSAGE_FORMAT)
    void requestInitialized(Object param1);

    @LogMessage(level = Level.TRACE)
    @Message(id = 709, value = "Destroying request {0}", format = Format.MESSAGE_FORMAT)
    void requestDestroyed(Object param1);

    @Message(id = 710, value = "Cannot inject {0} outside of a Servlet request", format = Format.MESSAGE_FORMAT)
    IllegalStateException cannotInjectObjectOutsideOfServletRequest(Object param1, @Cause Throwable cause);

    @LogMessage(level = Level.WARN)
    @Message(id = 711, value = "Context activation pattern {0} ignored as it is overriden by the integrator.", format = Format.MESSAGE_FORMAT)
    void webXmlMappingPatternIgnored(String pattern);

    @LogMessage(level = Level.WARN)
    @Message(id = 712, value = "Unable to dissociate context {0} when destroying request {1}", format = Format.MESSAGE_FORMAT)
    void unableToDissociateContext(Context context, HttpServletRequest request);

}