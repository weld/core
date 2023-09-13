/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

/**
 * Utils for working with PrivilegedActions, PrivilegedActionExceptions and AccessControllers.
 *
 * @author Jozef Hartinger
 *
 */
public class AccessControllers {

    private AccessControllers() {
    }

    /**
     * Removes the ambiguity between {@link PrivilegedAction} and {@link PrivilegedExceptionAction} that normally occurs when a
     * lambda is passed to
     * {@link AccessController#doPrivileged()}
     */
    public static <T> PrivilegedAction<T> action(PrivilegedAction<T> action) {
        return action;
    }

}
