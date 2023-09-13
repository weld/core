/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.exceptions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * An {@link java.lang.UnsupportedOperationException} with support for
 * localized messages in Weld.
 *
 * @author David Allen
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Workaround for exception classes poor i8ln support")
public class UnsupportedOperationException extends java.lang.UnsupportedOperationException {

    private static final long serialVersionUID = 2L;

    private final WeldExceptionMessage message;

    /**
     * Creates a new exception with no message. Here the stacktrace serves as the
     * main information since it has the method which was invoked causing this
     * exception.
     */
    public UnsupportedOperationException() {
        super();
        this.message = new WeldExceptionStringMessage("");
    }

    /**
     * Creates a new exception with the given localized message.
     *
     * @param message
     */
    public UnsupportedOperationException(String message) {
        this.message = new WeldExceptionStringMessage(message);
    }

    /**
     * Creates a new exception with the given localized message and the cause for this exception.
     *
     * @param message
     * @param throwable
     */
    public UnsupportedOperationException(String message, Throwable throwable) {
        super(throwable);
        this.message = new WeldExceptionStringMessage(message);
    }

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }

    @Override
    public String getMessage() {
        return message.getAsString();
    }
}
