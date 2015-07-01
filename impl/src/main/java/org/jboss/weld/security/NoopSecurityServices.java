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
package org.jboss.weld.security;

import java.security.Principal;

import org.jboss.weld.security.spi.SecurityServices;

/**
 * Fallback {@link SecurityServices} implementation that only used in the integrator does not provide one.
 * This implementation does not propagate security context.
 *
 * @author Jozef Hartinger
 *
 */
public class NoopSecurityServices implements SecurityServices {

    public static final SecurityServices INSTANCE = new NoopSecurityServices();

    private NoopSecurityServices() {
    }

    @Override
    public Principal getPrincipal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanup() {
    }
}
