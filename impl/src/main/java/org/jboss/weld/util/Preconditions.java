/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.util;

import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.logging.ValidatorLogger;

/**
 *
 * @author Jozef Hartinger
 *
 */
public class Preconditions {

    private Preconditions() {
    }

    /**
     * Throws {@link IllegalArgumentException} with an appropriate message if the reference is null.
     *
     * @param reference the reference to be checked
     * @param argumentName name of the argument that is being checked. The name used in the error message.
     */
    public static void checkArgumentNotNull(Object reference, String argumentName) {
        if (reference == null) {
            throw ValidatorLogger.LOG.argumentNull(argumentName);
        }
    }

    /**
     * Throws {@link IllegalArgumentException} if the reference is null. This method should only be used for methods with single
     * argument.
     *
     * @param reference the reference to be checked
     */
    public static void checkArgumentNotNull(Object reference) {
        if (reference == null) {
            throw ValidatorLogger.LOG.argumentNull();
        }
    }

    /**
     *
     * @param reference The reference to be checked
     * @throws NullPointerException if the reference is null
     */
    public static void checkNotNull(Object reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
    }

    public static void checkArgument(boolean condition, Object argument) {
        if (!condition) {
            throw new IllegalArgumentException("Illegal argument " + ((argument == null) ? "null" : argument.toString()));
        }
    }

    public static void checkArgument(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
