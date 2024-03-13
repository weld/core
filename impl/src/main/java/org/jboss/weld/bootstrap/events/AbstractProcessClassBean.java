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
package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.enterprise.invoke.Invoker;

import org.jboss.weld.bean.ClassBean;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.invokable.InvokerBuilderImpl;
import org.jboss.weld.invokable.TargetMethod;
import org.jboss.weld.invoke.WeldInvokerBuilder;
import org.jboss.weld.manager.BeanManagerImpl;

public abstract class AbstractProcessClassBean<X, B extends ClassBean<X>> extends AbstractDefinitionContainerEvent
        implements ProcessBean<X> {

    private final B bean;

    public AbstractProcessClassBean(BeanManagerImpl beanManager, Type rawType, Type[] actualTypeArguments, B bean) {
        super(beanManager, rawType, actualTypeArguments);
        this.bean = bean;
    }

    public Annotated getAnnotated() {
        checkWithinObserverNotification();
        return bean.getAnnotated();
    }

    public B getBean() {
        checkWithinObserverNotification();
        return bean;
    }

    public WeldInvokerBuilder<Invoker<X, ?>> createInvoker(AnnotatedMethod<? super X> annotatedMethod) {
        checkWithinObserverNotification();

        ClassBean<X> bean = getBean();
        if (bean instanceof Interceptor) {
            throw new DeploymentException("Cannot build invoker for an interceptor: " + bean);
        }
        if (bean instanceof Decorator) {
            throw new DeploymentException("Cannot build invoker for a decorator: " + bean);
        }

        Method method = annotatedMethod.getJavaMember();
        if (Modifier.isPrivate(method.getModifiers())) {
            throw new DeploymentException("Cannot build invoker for a private method: " + annotatedMethod);
        }
        if ("java.lang.Object".equals(method.getDeclaringClass().getName())
                && !"toString".equals(method.getName())) {
            throw new DeploymentException("Cannot build invoker for a method declared on java.lang.Object: " + annotatedMethod);
        }

        if (!bean.getAnnotated().getMethods().contains(annotatedMethod)) {
            throw new DeploymentException("Method does not belong to bean " + bean + ": " + annotatedMethod);
        }

        return new InvokerBuilderImpl<>(bean.getAnnotated(), new TargetMethod(annotatedMethod), getBeanManager());
    }

}
