/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import java.security.Permission;

/**
 * Utility class for checking {@link Permission}
 *
 * @author Matus Abaffy
 */
public class Permissions {

    public static final Permission MODIFY_THREAD_GROUP = new java.lang.RuntimePermission("modifyThreadGroup");

    private Permissions() {
    }

    /**
     * Determines whether the specified permission is permitted.
     *
     * @param permission
     * @return <tt>false<tt> if the specified permission is not permitted, based on the current security policy; <tt>true<tt>
     *         otherwise
     */
    public static boolean hasPermission(Permission permission) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            try {
                security.checkPermission(permission);
            } catch (java.security.AccessControlException e) {
                return false;
            }
        }
        return true;
    }
}
