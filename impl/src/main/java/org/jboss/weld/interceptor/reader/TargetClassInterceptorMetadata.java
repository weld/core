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

import java.util.List;
import java.util.Map;

import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorFactory;
import org.jboss.weld.interceptor.spi.metadata.MethodMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionType;

public class TargetClassInterceptorMetadata<T> extends AbstractInterceptorMetadata<T> {

    public TargetClassInterceptorMetadata(ClassMetadata<?> classMetadata, Map<InterceptionType, List<MethodMetadata>> interceptorMethodMap) {
        super(classMetadata, interceptorMethodMap);
    }

    @Override
    public InterceptorFactory<T> getInterceptorFactory() {
        return null;
    }

    @Override
    protected boolean isTargetClassInterceptor() {
        return true;
    }

}
