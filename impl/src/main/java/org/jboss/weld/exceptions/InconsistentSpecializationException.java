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
 * @author Pete Muir
 */
public class InconsistentSpecializationException extends DeploymentException {

    private static final long serialVersionUID = 4359656880524913555L;

    /**
     * Creates a new exception with the given cause.
     *
     * @param throwable The cause of the exception
     */
    public InconsistentSpecializationException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Creates a new exception with the given localized message.
     *
     * @param message
     */
    public InconsistentSpecializationException(String message) {
        super(message);
    }

}
