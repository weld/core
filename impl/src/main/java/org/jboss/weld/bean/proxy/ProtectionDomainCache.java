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
package org.jboss.weld.bean.proxy;

import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.weld.bootstrap.api.helpers.AbstractBootstrapService;

/**
 * Holds enhanced protection domains for proxy classes.
 *
 * This is only useful when running under security manager. The enhanced protection domain contains all the permissions of the
 * original class plus the "accessDeclaredMembers" runtime permission. This is needed for the proxy class' static initializer
 * block
 * to be able to resolve methods using {@link Class#getDeclaredMethod(String, Class...)}.
 *
 * @see BytecodeMethodResolver
 * @see WELD-1751
 *
 * @author Jozef Hartinger
 *
 */
public class ProtectionDomainCache extends AbstractBootstrapService {

    private static final Permission ACCESS_DECLARED_MEMBERS_PERMISSION = new RuntimePermission("accessDeclaredMembers");

    private final ConcurrentMap<CodeSource, ProtectionDomain> proxyProtectionDomains = new ConcurrentHashMap<CodeSource, ProtectionDomain>();

    /**
     * Gets an enhanced protection domain for a proxy based on the given protection domain.
     *
     * @param domain the given protection domain
     * @return protection domain enhanced with "accessDeclaredMembers" runtime permission
     */
    ProtectionDomain getProtectionDomainForProxy(ProtectionDomain domain) {
        if (domain.getCodeSource() == null) {
            // no codesource to cache on
            return create(domain);
        }
        ProtectionDomain proxyProtectionDomain = proxyProtectionDomains.get(domain.getCodeSource());
        if (proxyProtectionDomain == null) {
            // as this is not atomic create() may be called multiple times for the same domain
            // we ignore that
            proxyProtectionDomain = create(domain);
            ProtectionDomain existing = proxyProtectionDomains.putIfAbsent(domain.getCodeSource(), proxyProtectionDomain);
            if (existing != null) {
                proxyProtectionDomain = existing;
            }
        }
        return proxyProtectionDomain;
    }

    private ProtectionDomain create(ProtectionDomain domain) {
        if (domain.implies(ACCESS_DECLARED_MEMBERS_PERMISSION)) {
            return domain;
        }
        PermissionCollection permissions = domain.getPermissions();
        PermissionCollection proxyPermissions = new Permissions();

        if (permissions != null) {
            Enumeration<Permission> permissionElements = permissions.elements();
            while (permissionElements.hasMoreElements()) {
                proxyPermissions.add(permissionElements.nextElement());
            }
        }

        proxyPermissions.add(ACCESS_DECLARED_MEMBERS_PERMISSION);
        return new ProtectionDomain(domain.getCodeSource(), proxyPermissions);
    }

    @Override
    public void cleanupAfterBoot() {
        proxyProtectionDomains.clear();
    }

}
