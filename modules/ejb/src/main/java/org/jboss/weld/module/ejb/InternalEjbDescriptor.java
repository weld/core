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
package org.jboss.weld.module.ejb;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.ejb.spi.SubclassedComponentDescriptor;
import org.jboss.weld.ejb.spi.helpers.ForwardingEjbDescriptor;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Reflections;

/**
 * More powerful version of {@link EjbDescriptor} that exposes Maps for some
 * metadata. The {@link EjbDescriptor} to enhance should be passed to the
 * constructor
 *
 * @author Pete Muir
 */
class InternalEjbDescriptor<T> extends ForwardingEjbDescriptor<T> {

    static <T> InternalEjbDescriptor<T> of(EjbDescriptor<T> ejbDescriptor) {
        if (ejbDescriptor instanceof InternalEjbDescriptor<?>) {
            return cast(ejbDescriptor);
        }
        return new InternalEjbDescriptor<T>(ejbDescriptor);
    }

    private final Class<?> objectInterface;
    private final EjbDescriptor<T> delegate;
    private final Collection<MethodSignature> removeMethodSignatures;
    private final Set<Class<?>> localBusinessInterfaces;
    private final Set<Class<?>> remoteBusinessInterfaces;

    private InternalEjbDescriptor(EjbDescriptor<T> ejbDescriptor) {
        this.delegate = ejbDescriptor;
        this.objectInterface = findObjectInterface(ejbDescriptor.getLocalBusinessInterfaces());
        removeMethodSignatures = new ArrayList<MethodSignature>();
        if (ejbDescriptor.getRemoveMethods() != null) {
            for (Method method : ejbDescriptor.getRemoveMethods()) {
                removeMethodSignatures.add(new MethodSignatureImpl(method));
            }
        }
        this.localBusinessInterfaces = transformToClasses(getLocalBusinessInterfaces());
        this.remoteBusinessInterfaces = transformToClasses(getRemoteBusinessInterfaces());
    }

    private static Set<Class<?>> transformToClasses(Collection<BusinessInterfaceDescriptor<?>> interfaceDescriptors) {
        if (interfaceDescriptors == null) {
            return Collections.emptySet();
        }
        return interfaceDescriptors.stream().map(d -> d.getInterface()).collect(ImmutableSet.collector());
    }

    @Override
    public EjbDescriptor<T> delegate() {
        return delegate;
    }

    public Class<?> getObjectInterface() {
        return objectInterface;
    }

    public Collection<MethodSignature> getRemoveMethodSignatures() {
        return removeMethodSignatures;
    }

    private static Class<?> findObjectInterface(Collection<BusinessInterfaceDescriptor<?>> interfaces) {
        if (interfaces != null && !interfaces.isEmpty()) {
            return interfaces.iterator().next().getInterface();
        } else {
            return null;
        }
    }

    public Set<Class<?>> getLocalBusinessInterfacesAsClasses() {
        return localBusinessInterfaces;
    }

    public Set<Class<?>> getRemoteBusinessInterfacesAsClasses() {
        return remoteBusinessInterfaces;
    }

    public Class<? extends T> getImplementationClass() {
        if (delegate instanceof SubclassedComponentDescriptor) {
            SubclassedComponentDescriptor<T> descriptor = Reflections.<SubclassedComponentDescriptor<T>> cast(delegate);
            Class<? extends T> implementationClass = descriptor.getComponentSubclass();
            if (implementationClass != null) {
                return implementationClass;
            }
        }
        return delegate.getBeanClass();
    }
}
