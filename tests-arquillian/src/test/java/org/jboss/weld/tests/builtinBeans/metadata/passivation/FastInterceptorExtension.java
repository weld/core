/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.builtinBeans.metadata.passivation;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionTargetFactory;
import javax.enterprise.inject.spi.InterceptionType;
import javax.interceptor.InvocationContext;

/**
 * Register {@link FastInterceptor} as an interceptor using {@link AbstractPassivationCapableInterceptorImpl}.
 *
 * @author Jozef Hartinger
 *
 */
public class FastInterceptorExtension implements Extension {

    void registerInterceptor(@Observes AfterBeanDiscovery event, BeanManager manager) {
        AnnotatedType<FastInterceptor> annotatedType = manager.createAnnotatedType(FastInterceptor.class);
        BeanAttributes<FastInterceptor> attributes = manager.createBeanAttributes(annotatedType);
        Set<Annotation> interceptorBindings = Collections.<Annotation> singleton(Fast.Literal.INSTANCE);
        Set<InterceptionType> interceptionTypes = Collections.singleton(InterceptionType.AROUND_INVOKE);
        InjectionTargetFactory<FastInterceptor> factory = manager.getInjectionTargetFactory(annotatedType);
        event.addBean(new AbstractPassivationCapableInterceptorImpl<FastInterceptor>(FastInterceptor.class, attributes,
                interceptorBindings, interceptionTypes, factory) {
            @Override
            public Object intercept(InterceptionType type, FastInterceptor instance, InvocationContext ctx) throws Exception {
                return instance; // instead of intercepting return the interceptor instance so that we can examine its state
            }
        });
    }
}
