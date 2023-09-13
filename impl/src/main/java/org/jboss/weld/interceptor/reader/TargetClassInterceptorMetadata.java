/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.interceptor.reader;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 * Component's target class interceptor metadata. This class is immutable.
 *
 * @author Jozef Hartinger
 *
 */
public class TargetClassInterceptorMetadata extends AbstractInterceptorMetadata {

    public static final TargetClassInterceptorMetadata EMPTY_INSTANCE = new TargetClassInterceptorMetadata(
            Collections.<InterceptionType, List<Method>> emptyMap());

    public static TargetClassInterceptorMetadata of(Map<InterceptionType, List<Method>> interceptorMethodMap) {
        if (interceptorMethodMap.isEmpty()) {
            return EMPTY_INSTANCE;
        }
        return new TargetClassInterceptorMetadata(interceptorMethodMap);
    }

    private final Set<Method> interceptorMethods;

    private TargetClassInterceptorMetadata(Map<InterceptionType, List<Method>> interceptorMethodMap) {
        super(interceptorMethodMap);
        this.interceptorMethods = initInterceptorMethods(interceptorMethodMap);
    }

    private Set<Method> initInterceptorMethods(Map<InterceptionType, List<Method>> interceptorMethodMap) {
        ImmutableSet.Builder<Method> builder = ImmutableSet.builder();
        for (List<Method> methodList : interceptorMethodMap.values()) {
            builder.addAll(methodList);
        }
        return builder.build();
    }

    @Override
    protected boolean isTargetClassInterceptor() {
        return true;
    }

    public boolean isInterceptorMethod(Method method) {
        return interceptorMethods.contains(method);
    }
}
