/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.util.reflection.instantiation;

/**
 * An interface for instantiating classes using non-portable reflection methods
 *
 * @author Nicklas Karlsson
 */
public interface Instantiator {
    /**
     * Create a new instance of a class
     *
     * @param <T>   The type of the class
     * @param clazz The class
     * @return The created instance
     */
    <T> T instantiate(Class<T> clazz);

    /**
     * Used for checking if this particular instantiation method is available in the environment
     *
     * @return True if available, false otherwise
     */
    boolean isAvailable();
}
