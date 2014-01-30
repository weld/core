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
package org.jboss.weld.annotated;

import java.io.Serializable;

import javax.enterprise.inject.spi.PassivationCapable;

/**
 * Marker interface for an identifier. The identifier has to be mutable and serializable.
 *
 * @author Jozef Hartinger
 *
 */
public interface Identifier extends Serializable {

    String ID_SEPARATOR = "|";

    /**
     * String representation of this identifier. This is required as some parts of the CDI API use String identifiers, for
     * example {@link PassivationCapable#getId()}. Unlike {@link #toString()}, this method returns a non-verbose canonical
     * string identifier.
     *
     * @return
     */
    String asString();

}
