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

import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;

/**
 * JBoss Logging is not using message keys and so it's not possible to reference the log message (a logger method annotated with
 * {@link LogMessage} and
 * {@link Message}) and use it as a method parameter or construct it lazily. This callback should be used to work around this
 * limitation.
 *
 * Note that the method parameters are not checked and so the invocation may result in {@link ArrayIndexOutOfBoundsException}.
 *
 * @author Martin Kouba
 * @see LogMessage
 */
@FunctionalInterface
public interface LogMessageCallback {

    /**
     * Invokes the logger method.
     *
     * @param params
     */
    void log(Object... params);

}
