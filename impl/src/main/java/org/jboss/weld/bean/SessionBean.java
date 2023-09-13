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
package org.jboss.weld.bean;

import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.ejb.spi.EjbDescriptor;

/**
 * {@link Bean} implementation representing an enterprise session bean.
 *
 * @author Jozef Hartinger
 *
 * @param <T> the type of the bean instance
 */
public interface SessionBean<T> extends ClassBean<T> {

    /**
     * Returns an EJB descriptor for this bean
     *
     * @return EJB descriptor
     */
    EjbDescriptor<T> getEjbDescriptor();

    /**
     * Returns an unmodifiable set of local business method signatures.
     *
     * @return business method signatures
     */
    Set<MethodSignature> getLocalBusinessMethodSignatures();

    /**
     * Returns an unmodifiable set of remote business method signatures.
     *
     * @return business method signatures
     */
    Set<MethodSignature> getRemoteBusinessMethodSignatures();
}
