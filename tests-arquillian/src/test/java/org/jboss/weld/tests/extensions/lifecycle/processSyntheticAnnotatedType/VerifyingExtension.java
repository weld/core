/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.tests.extensions.lifecycle.processSyntheticAnnotatedType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.FixedProcessSyntheticAnnotatedType;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

public class VerifyingExtension implements Extension {

    private Set<Class<?>> patClasses = new HashSet<Class<?>>();
    private Set<Class<?>> psatClasses = new HashSet<Class<?>>();
    private Map<Class<?>, Extension> sources = new HashMap<Class<?>, Extension>();

    <T> void verify(@Observes ProcessAnnotatedType<T> event) {
        if (event instanceof FixedProcessSyntheticAnnotatedType<?>) {
            psatClasses.add(event.getAnnotatedType().getJavaClass());
        } else {
            patClasses.add(event.getAnnotatedType().getJavaClass());
        }
    }

    <T> void verifySource(@Observes FixedProcessSyntheticAnnotatedType<T> event) {
        sources.put(event.getAnnotatedType().getJavaClass(), event.getSource());
    }

    protected Set<Class<?>> getPatClasses() {
        return patClasses;
    }

    protected Set<Class<?>> getPsatClasses() {
        return psatClasses;
    }

    protected Map<Class<?>, Extension> getSources() {
        return sources;
    }
}
