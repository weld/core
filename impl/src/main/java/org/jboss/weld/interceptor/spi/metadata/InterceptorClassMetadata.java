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

package org.jboss.weld.interceptor.spi.metadata;

import java.io.Serializable;

/**
 * Metadata about an interceptor component.
 *
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 * @author Jozef Hartinger
 *
 * @param <T> The type (class) of the interceptor bean
 */
public interface InterceptorClassMetadata<T> extends InterceptorMetadata {

    /**
     * Returns an InterceptorFactory capable of creating instances of this interceptor..
     *
     * @return the interceptor factory
     */
    InterceptorFactory<T> getInterceptorFactory();

    /**
     * Returns the class of this interceptor.
     *
     * @return
     */
    Class<T> getJavaClass();

    /**
     *
     * @return a unique key which might be used to identify a metadata instance
     */
    default Serializable getKey() {
        return getJavaClass();
    }
}
