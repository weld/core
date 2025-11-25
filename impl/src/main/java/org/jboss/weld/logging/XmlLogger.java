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

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.IllegalStateException;

/**
 * Error messages relating to XML parser
 *
 * Message ids: 001200 - 001299
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface XmlLogger extends WeldLogger {

    XmlLogger LOG = Logger.getMessageLogger(MethodHandles.lookup(), XmlLogger.class, Category.BOOTSTRAP.getName());

    @Message(id = 1200, value = "Error configuring XML parser")
    IllegalStateException configurationError(@Cause Throwable cause);

    @Message(id = 1201, value = "Error loading beans.xml {0}", format = Format.MESSAGE_FORMAT)
    IllegalStateException loadError(Object param1, @Cause Throwable cause);

    @Message(id = 1202, value = "Error parsing {0}", format = Format.MESSAGE_FORMAT)
    IllegalStateException parsingError(Object param1, @Cause Throwable cause);

    @Message(id = 1203, value = "<alternatives> can only be specified once, but appears multiple times:  {0}", format = Format.MESSAGE_FORMAT)
    DefinitionException multipleAlternatives(Object param1);

    @Message(id = 1204, value = "<decorators> can only be specified once, but is specified multiple times:  {0}", format = Format.MESSAGE_FORMAT)
    DefinitionException multipleDecorators(Object param1);

    @Message(id = 1205, value = "<interceptors> can only be specified once, but it is specified multiple times:  {0}", format = Format.MESSAGE_FORMAT)
    DefinitionException multipleInterceptors(Object param1);

    @Message(id = 1207, value = "<scan> can only be specified once, but it is specified multiple times:  {0}", format = Format.MESSAGE_FORMAT)
    DefinitionException multipleScanning(Object param1);

    @LogMessage(level = Level.WARN)
    @Message(id = 1208, value = "Error when validating {0}@{1} against xsd. {2}", format = Format.MESSAGE_FORMAT)
    void xsdValidationError(Object param1, Object param2, Object param3);

    @LogMessage(level = Level.WARN)
    @Message(id = 1210, value = "Warning when validating {0}@{1} against xsd. {2}", format = Format.MESSAGE_FORMAT)
    void xsdValidationWarning(Object param1, Object param2, Object param3);

}
