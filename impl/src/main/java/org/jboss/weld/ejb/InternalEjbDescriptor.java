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
package org.jboss.weld.ejb;

import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.ejb.spi.helpers.ForwardingEjbDescriptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

/**
 * More powerful version of {@link EjbDescriptor} that exposes Maps for some
 * metadata. The {@link EjbDescriptor} to enhance should be passed to the
 * constructor
 *
 * @author Pete Muir
 */
public class InternalEjbDescriptor<T> extends ForwardingEjbDescriptor<T> {

    private final Class<?> objectInterface;
    private final EjbDescriptor<T> delegate;
    private final Collection<MethodSignature> removeMethodSignatures;

    public InternalEjbDescriptor(EjbDescriptor<T> ejbDescriptor) {
        this.delegate = ejbDescriptor;
        this.objectInterface = findObjectInterface(ejbDescriptor.getLocalBusinessInterfaces());
        removeMethodSignatures = new ArrayList<MethodSignature>();
        if (ejbDescriptor.getRemoveMethods() != null) {
            for (Method method : ejbDescriptor.getRemoveMethods()) {
                removeMethodSignatures.add(new MethodSignatureImpl(method));
            }
        }
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

}
