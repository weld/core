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
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;

/**
 * Log messages for EL resolution.
 *
 * Message IDs: 001000 - 001099
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface ElLogger extends WeldLogger {

    ElLogger LOG = Logger.getMessageLogger(MethodHandles.lookup(), ElLogger.class, Category.EL.getName());

    @Message(id = 1001, value = "Cannot pass null expressionFactory")
    IllegalArgumentException nullExpressionFactory();

    @LogMessage(level = Level.TRACE)
    @Message(id = 1002, value = "Looking for EL property {0}", format = Format.MESSAGE_FORMAT)
    void propertyLookup(Object param1);

    @LogMessage(level = Level.TRACE)
    @Message(id = 1003, value = "EL property {0} resolved to {1}", format = Format.MESSAGE_FORMAT)
    void propertyResolved(Object param1, Object param2);

}
