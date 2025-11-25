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
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.exceptions.WeldException;

/**
 * Log messages for resolution classes.
 *
 * Message IDs: 001600 - 001699
 *
 * Note that original message ID range (1200-1299) was in conflict with {@link XmlLogger}.
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface ResolutionLogger extends WeldLogger {

    ResolutionLogger LOG = Logger.getMessageLogger(MethodHandles.lookup(), ResolutionLogger.class,
            Category.RESOLUTION.getName());

    @Message(id = 1601, value = "Cannot extract rawType from {0}", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException cannotExtractRawType(Object param1);

    @Message(id = 1602, value = "Cannot create qualifier instance model for {0}\n\tat {1}\n  StackTrace:", format = Format.MESSAGE_FORMAT)
    WeldException cannotCreateQualifierInstanceValues(Object annotation, Object stackElement, @Cause Exception cause);

}
