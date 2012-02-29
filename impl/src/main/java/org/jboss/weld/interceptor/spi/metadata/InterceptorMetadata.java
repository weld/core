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
import java.util.List;

import org.jboss.weld.interceptor.spi.model.InterceptionType;

/**
 * This class is parametrized for
 *
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public interface InterceptorMetadata<T> extends Serializable {
    /**
     * Returns the class for which this interceptor metadata was created
     *
     * @return
     */
    InterceptorReference<T> getInterceptorReference();

    ClassMetadata<?> getInterceptorClass();

    /**
     * Returns the list of interceptor methods of this class for a given
     * interception type.
     *
     * @param interceptionType
     * @return a list of methods
     */
    List<MethodMetadata> getInterceptorMethods(InterceptionType interceptionType);

    /**
     * Returns true if the interceptor corresponding to this {@link InterceptorMetadata}
     * has interceptor methods for the given <code>interceptionType</code>. Else returns false.
     *
     * @param interceptionType The {@link org.jboss.weld.interceptor.spi.model.InterceptionType}
     * @return
     */
    boolean isEligible(InterceptionType interceptionType);

    boolean isTargetClass();
}
