/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
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

package org.jboss.interceptor.reader;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.interceptor.spi.metadata.ClassMetadata;
import org.jboss.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.interceptor.spi.metadata.InterceptorReference;
import org.jboss.interceptor.spi.metadata.MethodMetadata;
import org.jboss.interceptor.spi.model.InterceptionType;


/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public class SimpleInterceptorMetadata<T> implements InterceptorMetadata<T>, Serializable {

    private static final long serialVersionUID = 1247010247012491L;

    private Map<InterceptionType, List<MethodMetadata>> interceptorMethodMap;

    private final InterceptorReference<T> interceptorReference;

    private boolean targetClass;

    public SimpleInterceptorMetadata(InterceptorReference<T> interceptorReference, boolean targetClass, Map<InterceptionType, List<MethodMetadata>> interceptorMethodMap) {
        this.interceptorReference = interceptorReference;
        this.targetClass = targetClass;
        this.interceptorMethodMap = interceptorMethodMap;
    }

    /**
     * {@inheritDoc}
     */
    public ClassMetadata<?> getInterceptorClass() {
        return this.interceptorReference.getClassMetadata();
    }

    public InterceptorReference<T> getInterceptorReference() {
        return interceptorReference;
    }

    public boolean isTargetClass() {
        return targetClass;
    }

    public List<MethodMetadata> getInterceptorMethods(InterceptionType interceptionType) {
        if (interceptorMethodMap != null) {
            List<MethodMetadata> methods = interceptorMethodMap.get(interceptionType);
            return methods == null ? Collections.<MethodMetadata>emptyList() : methods;
        } else {
            return Collections.<MethodMetadata>emptyList();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEligible(InterceptionType interceptionType) {
        if (this.interceptorMethodMap == null) {
            return false;
        }
        List<MethodMetadata> interceptorMethods = this.interceptorMethodMap.get(interceptionType);
        // return true if there are any interceptor methods for this interception type
        return interceptorMethods != null && interceptorMethods.isEmpty() == false;
    }

}
