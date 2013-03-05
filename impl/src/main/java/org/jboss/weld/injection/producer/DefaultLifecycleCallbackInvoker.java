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
package org.jboss.weld.injection.producer;

import static org.jboss.weld.logging.messages.BeanMessage.INVOCATION_ERROR;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.spi.AnnotatedMethod;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.runtime.RuntimeAnnotatedMembers;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.interceptor.util.InterceptionUtils;
import org.jboss.weld.util.BeanMethods;

/**
 * If the component is not intercepted this implementation takes care of invoking its lifecycle callback methods. If the
 * component is interception, {@link PostConstruct} / {@link PreDestroy} invocation is delegated to the intercepting proxy.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class DefaultLifecycleCallbackInvoker<T> implements LifecycleCallbackInvoker<T> {

    private final List<AnnotatedMethod<? super T>> postConstructMethods;
    private final List<AnnotatedMethod<? super T>> preDestroyMethods;

    public DefaultLifecycleCallbackInvoker(EnhancedAnnotatedType<T> type) {
        this.postConstructMethods = BeanMethods.getPostConstructMethods(type);
        this.preDestroyMethods = BeanMethods.getPreDestroyMethods(type);
    }

    @Override
    public void postConstruct(T instance, Instantiator<T> instantiator) {
        if (instantiator.hasInterceptorSupport()) {
            InterceptionUtils.executePostConstruct(instance);
        } else {
            invokeMethods(postConstructMethods, instance);
        }
    }

    @Override
    public void preDestroy(T instance, Instantiator<T> instantiator) {
        if (instantiator.hasInterceptorSupport()) {
            InterceptionUtils.executePredestroy(instance);
        } else {
            invokeMethods(preDestroyMethods, instance);
        }
    }

    protected void invokeMethods(List<AnnotatedMethod<? super T>> methods, T instance) {
        for (AnnotatedMethod<? super T> method : methods) {
            try {
                RuntimeAnnotatedMembers.invokeMethod(method, instance);
            } catch (Exception e) {
                throw new WeldException(INVOCATION_ERROR, e, method, instance);
            }
        }
    }

    @Override
    public List<AnnotatedMethod<? super T>> getPostConstructMethods() {
        return postConstructMethods;
    }

    @Override
    public List<AnnotatedMethod<? super T>> getPreDestroyMethods() {
        return preDestroyMethods;
    }
}