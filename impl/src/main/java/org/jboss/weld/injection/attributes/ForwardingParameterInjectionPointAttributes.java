/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.injection.attributes;

import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.util.reflection.Reflections;

/**
 * An implementation of {@link WeldInjectionPointAttributes} that forwards calls to an extension-provided {@link InjectionPoint}
 * implementation.
 *
 * @author Jozef Hartinger
 *
 */
public class ForwardingParameterInjectionPointAttributes<T, X> extends AbstractForwardingInjectionPointAttributes<T, Object>
        implements ParameterInjectionPointAttributes<T, X> {

    public static <T, X> ForwardingParameterInjectionPointAttributes<T, X> of(InjectionPoint ip) {
        if (ip instanceof ForwardingParameterInjectionPointAttributes<?, ?>) {
            return Reflections.cast(ip);
        }
        if (!(ip.getAnnotated() instanceof AnnotatedParameter<?>)) {
            throw BeanLogger.LOG.invalidInjectionPointType(ForwardingParameterInjectionPointAttributes.class,
                    ip.getAnnotated());
        }
        return new ForwardingParameterInjectionPointAttributes<T, X>(ip);
    }

    private static final long serialVersionUID = 6109999203440035470L;

    protected ForwardingParameterInjectionPointAttributes(InjectionPoint delegate) {
        super(delegate);
    }

    @Override
    public AnnotatedParameter<X> getAnnotated() {
        return Reflections.cast(delegate().getAnnotated()); // checked in initializer
    }
}
