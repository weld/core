/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.environment.logging.WeldEnvironmentLogger;

/**
 *
 * Message IDs: 001300 - 001399
 *
 * @author Jozef Hartinger
 *
 */
@MessageLogger(projectCode = WeldEnvironmentLogger.WELD_ENV_PROJECT_CODE)
public interface UndertowLogger extends WeldEnvironmentLogger {
    UndertowLogger LOG = Logger.getMessageLogger(UndertowLogger.class, Category.UNDERTOW.getName());

    @LogMessage(level = Level.INFO)
    @Message(id = 1300, value = "Undertow detected, CDI injection will be available in Servlets.")
    void undertowDetectedServletOnly();

    @LogMessage(level = Level.DEBUG)
    @Message(id = 1301, value = "Installing CDI support for {0}", format = Format.MESSAGE_FORMAT)
    void installingCdiSupport(Class<?> clazz);

    @LogMessage(level = Level.INFO)
    @Message(id = 1302, value = "Undertow detected, CDI injection will be available in Servlets, Filters and Listeners.")
    void undertowDetected();
}
