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


/**
 * Thrown if a simple bean is dependent scoped and injected into a stateful
 * session bean, into a non-transient field, bean constructor parameter or
 * initializer method parameter of a bean which declares a passivating scope, or
 * into a parameter of a producer method which declares a passivating scope
 *
 * @author Pete Muir
 */
public class UnserializableDependencyException extends DeploymentException {

    private static final long serialVersionUID = -6287506607413810688L;

    /**
     * Creates a new exception with the given localized message key and optional
     * arguments for the message.
     *
     * @param <E>  The enumeration type for the message keys
     * @param key  The localized message to use
     * @param args Optional arguments to insert into the message
     */
    public <E extends Enum<?>> UnserializableDependencyException(E key, Object... args) {
        super(key, args);
    }

    /**
     * Creates a new exception with the given cause.
     *
     * @param throwable The cause of the exception
     */
    public UnserializableDependencyException(Throwable throwable) {
        super(throwable);
    }


}
