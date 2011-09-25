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
package org.jboss.weld.util.bytecode;

import java.lang.reflect.Method;

/**
 * Data that is needed when working with a method in bytecode
 *
 * @author Stuart Douglas
 */
public interface MethodInformation {

    /**
     * The declaring class name in java dotted form (e.g. java.lang.String)
     */
    String getDeclaringClass();

    /**
     * This may return null if Method is not available yet
     */
    Method getMethod();

    /**
     * Gets the method descriptor
     */
    String getDescriptor();

    /**
     * returns string representations of the parameter types
     */
    String[] getParameterTypes();

    /**
     * Gets the method return type, in descriptor format (e.g. Ljava/lang/String;
     * )
     */
    String getReturnType();

    /**
     * the method name
     */
    String getName();

    /**
     * The method modifiers
     *
     * @return The modifiers
     */
    int getModifiers();

}
