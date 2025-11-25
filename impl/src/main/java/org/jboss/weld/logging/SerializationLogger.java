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
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.exceptions.InvalidObjectException;

/**
 * Log messages for serialization.
 *
 * Message ids: 001800 - 001899
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface SerializationLogger extends WeldLogger {

    SerializationLogger LOG = Logger.getMessageLogger(MethodHandles.lookup(), SerializationLogger.class,
            Category.SERIALIZATION.getName());

    @Message(id = 1800, value = "Unable to get bean identifier at position {0} from {1}", format = Format.MESSAGE_FORMAT)
    IllegalStateException unableToGetBeanIdentifier(int index, Object info);

    @Message(id = 1801, value = "Unable to deserialize {0}", format = Format.MESSAGE_FORMAT)
    InvalidObjectException unableToDeserialize(Object info, @Cause Throwable cause);

}
