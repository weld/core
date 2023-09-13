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
package org.jboss.weld.injection;

import static org.jboss.weld.injection.Exceptions.rethrowException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;

import jakarta.enterprise.inject.spi.AnnotatedMethod;

import org.jboss.weld.bean.proxy.DecoratorProxy;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.security.GetAccessibleCopyOfMember;
import org.jboss.weld.util.reflection.Reflections;

/**
 * High-level representation of a resource setter method.
 *
 * @author Martin Kouba
 *
 * @param <T>
 * @param <X>
 */
class SetterResourceInjection<T, X> extends AbstractResourceInjection<T> {

    private final Method accessibleMethod;

    /**
     *
     * @param injectionPoint
     * @param factory
     */
    SetterResourceInjection(ParameterInjectionPoint<T, X> injectionPoint, ResourceReferenceFactory<T> factory) {
        super(factory);
        AnnotatedMethod<X> annotatedMethod = (AnnotatedMethod<X>) injectionPoint.getAnnotated().getDeclaringCallable();
        accessibleMethod = AccessController
                .doPrivileged(new GetAccessibleCopyOfMember<Method>(annotatedMethod.getJavaMember()));
    }

    @Override
    protected void injectMember(Object declaringInstance, Object reference) {
        try {
            Object instanceToInject = declaringInstance;
            if (!(instanceToInject instanceof DecoratorProxy)) {
                // if declaringInstance is a proxy, unwrap it
                if (instanceToInject instanceof TargetInstanceProxy) {
                    instanceToInject = Reflections.<TargetInstanceProxy<T>> cast(declaringInstance).weld_getTargetInstance();
                }
            }
            accessibleMethod.invoke(instanceToInject, reference);
        } catch (IllegalArgumentException e) {
            rethrowException(e);
        } catch (IllegalAccessException e) {
            rethrowException(e);
        } catch (InvocationTargetException e) {
            rethrowException(e);
        }
    }

    @Override
    Method getMember() {
        return accessibleMethod;
    }

}