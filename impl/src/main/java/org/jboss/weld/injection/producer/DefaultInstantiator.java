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
package org.jboss.weld.injection.producer;

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.INJECTION_TARGET_CREATED_FOR_CLASS_WITHOUT_APPROPRIATE_CONSTRUCTOR;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.exceptions.CreationException;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.injection.AroundConstructCallback;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.logging.messages.BeanMessage;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.slf4j.cal10n.LocLogger;

/**
 * Creates a new Java object by calling its class constructor. This class is thread-safe.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class DefaultInstantiator<T> extends AbstractInstantiator<T> {

    private static final LocLogger log = loggerFactory().getLogger(BEAN);

    private final ConstructorInjectionPoint<T> constructor;
    private final boolean producible;

    public DefaultInstantiator(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl manager) {
        if (type.getJavaClass().isInterface()) {
            throw new DefinitionException(BeanMessage.INJECTION_TARGET_CANNOT_BE_CREATED_FOR_INTERFACE, type);
        }
        boolean producible = true;
        if (type.isAbstract()) {
            /*
             * We could be strict here and throw an error but there are certain extension (e.g. Solder)
             * which rely on this so in order not to break them we only display a warning.
             */
            log.warn(BeanMessage.INJECTION_TARGET_CREATED_FOR_ABSTRACT_CLASS, type.getJavaClass());
            producible = false;
        }
        EnhancedAnnotatedConstructor<T> constructor = Beans.getBeanConstructor(type);
        if (constructor == null) {
            if (bean != null) {
                throw new DefinitionException(INJECTION_TARGET_CREATED_FOR_CLASS_WITHOUT_APPROPRIATE_CONSTRUCTOR, type.getJavaClass());
            } else {
                producible = false;
                this.constructor = null;
                log.warn(INJECTION_TARGET_CREATED_FOR_CLASS_WITHOUT_APPROPRIATE_CONSTRUCTOR, type.getJavaClass());
            }
        } else {
            this.constructor = InjectionPointFactory.instance().createConstructorInjectionPoint(bean, type.getJavaClass(), constructor, manager);
        }
        this.producible = producible;
    }

    @Override
    public T newInstance(CreationalContext<T> ctx, BeanManagerImpl manager, AroundConstructCallback<T> callback) {
        if (!producible) {
            throw new CreationException(BeanMessage.INJECTION_TARGET_CANNOT_PRODUCE_INSTANCE);
        }
        return super.newInstance(ctx, manager, callback);
    }

    @Override
    public ConstructorInjectionPoint<T> getConstructorInjectionPoint() {
        return constructor;
    }

    @Override
    public Constructor<T> getConstructor() {
        if (constructor == null) { // TODO: isolate this into a special non-producible instantiator
            return null;
        }
        return constructor.getAnnotated().getJavaMember();
    }

    public List<ParameterInjectionPoint<?, T>> getParameterInjectionPoints() {
        if (constructor == null) {
            return Collections.emptyList();
        }
        return constructor.getParameterInjectionPoints();
    }

    @Override
    public String toString() {
        return "SimpleInstantiator [constructor=" + constructor.getMember() + "]";
    }

    @Override
    public boolean hasInterceptorSupport() {
        return false;
    }

    @Override
    public boolean hasDecoratorSupport() {
        return false;
    }
}
