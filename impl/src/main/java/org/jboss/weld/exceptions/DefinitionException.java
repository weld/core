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

import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Thrown if the definition of a bean is incorrect
 *
 * @author Pete Muir
 * @author Jozef Hartinger
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Renaming the class would break backwards compatibility.")
public class DefinitionException extends jakarta.enterprise.inject.spi.DefinitionException {
    private static final long serialVersionUID = 8014646336322875707L;

    private final WeldExceptionMessage message;

    /**
     * Creates a new exception with the given cause.
     *
     * @param throwable The cause of the exception
     */
    public DefinitionException(Throwable throwable) {
        super(throwable);
        this.message = new WeldExceptionStringMessage(throwable.getLocalizedMessage());
    }

    /**
     * Creates a new exception based on a list of throwables. The throwables are not
     * used as the cause, but the message from each throwable is included as the message
     * for this exception.
     *
     * @param errors A list of throwables to use in the message
     */
    public DefinitionException(List<Throwable> errors) {
        super(DefinitionException.class.getName()); // the no-args constructor is missing
        this.message = new WeldExceptionListMessage(errors);
        for (Throwable error : errors) {
            addSuppressed(error);
        }
    }

    /**
     * Creates a new exception with the given localized message.
     *
     * @param message
     */
    public DefinitionException(String message) {
        super(DefinitionException.class.getName()); // the no-args constructor is missing
        this.message = new WeldExceptionStringMessage(message);
    }

    /**
     * Creates a new exception with the given localized message and the cause for this exception.
     *
     * @param message
     * @param throwable
     */
    public DefinitionException(String message, Throwable throwable) {
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
