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
package org.jboss.weld.injection;

import java.lang.reflect.InvocationTargetException;

import jakarta.enterprise.inject.CreationException;

import org.jboss.weld.exceptions.WeldException;

class Exceptions {

    private Exceptions() {
    }

    private static void rethrowException(Throwable t, Class<? extends RuntimeException> exceptionToThrow) {
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else {
            RuntimeException e;
            try {
                e = exceptionToThrow.getDeclaredConstructor().newInstance();
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                throw new WeldException(ex.getCause());
            }
            e.initCause(t);
            throw e;
        }
    }

    private static void rethrowException(Throwable t) {
        rethrowException(t, CreationException.class);
    }

    public static void rethrowException(IllegalArgumentException e) {
        rethrowException(e.getCause() != null ? e.getCause() : e);
    }

    public static void rethrowException(IllegalArgumentException e, Class<? extends RuntimeException> exceptionToThrow) {
        rethrowException(e.getCause() != null ? e.getCause() : e, exceptionToThrow);
    }

    public static void rethrowException(InstantiationException e, Class<? extends RuntimeException> exceptionToThrow) {
        rethrowException(e.getCause() != null ? e.getCause() : e, exceptionToThrow);
    }

    public static void rethrowException(InstantiationException e) {
        rethrowException(e.getCause() != null ? e.getCause() : e);
    }

    public static void rethrowException(IllegalAccessException e) {
        rethrowException(e.getCause() != null ? e.getCause() : e);
    }

    public static void rethrowException(IllegalAccessException e, Class<? extends RuntimeException> exceptionToThrow) {
        rethrowException(e.getCause() != null ? e.getCause() : e, exceptionToThrow);
    }

    public static void rethrowException(InvocationTargetException e, Class<? extends RuntimeException> exceptionToThrow) {
        rethrowException(e.getCause() != null ? e.getCause() : e, exceptionToThrow);
    }

    public static void rethrowException(SecurityException e, Class<? extends RuntimeException> exceptionToThrow) {
        rethrowException(e.getCause() != null ? e.getCause() : e, exceptionToThrow);
    }

    public static void rethrowException(NoSuchMethodException e, Class<? extends RuntimeException> exceptionToThrow) {
        rethrowException(e.getCause() != null ? e.getCause() : e, exceptionToThrow);
    }

    public static void rethrowException(InvocationTargetException e) {
        rethrowException(e.getCause() != null ? e.getCause() : e);
    }

}
