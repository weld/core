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
package org.jboss.weld.bean.builtin;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Decorated;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.bean.BeanIdentifiers;
import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.contexts.WeldCreationalContext;
import org.jboss.weld.literal.DecoratedLiteral;
import org.jboss.weld.logging.InterceptorLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.bean.SerializableForwardingBean;

/**
 * Allows a decorator to obtain information about the bean it decorates.
 *
 * @author Jozef Hartinger
 * @see CDI-92
 *
 */
public class DecoratedBeanMetadataBean extends InterceptedBeanMetadataBean {

    public DecoratedBeanMetadataBean(BeanManagerImpl beanManager) {
        super(new StringBeanIdentifier(
                BeanIdentifiers.forBuiltInBean(beanManager, Bean.class, Decorated.class.getSimpleName())), beanManager);
    }

    @Override
    protected void checkInjectionPoint(InjectionPoint ip) {
        if (!(ip.getBean() instanceof Decorator<?>)) {
            throw new IllegalArgumentException("@Decorated Bean<?> can only be injected into a decorator.");
        }
    }

    @Override
    protected Bean<?> newInstance(InjectionPoint ip, CreationalContext<Bean<?>> ctx) {
        checkInjectionPoint(ip);

        WeldCreationalContext<?> decoratorContext = getParentCreationalContext(ctx);
        WeldCreationalContext<?> beanContext = getParentCreationalContext(decoratorContext);
        // when there are more decorators present, a CreationalContext hierarchy is created between them
        // we want to iterate over this hierarchy to make sure we return the original decorated bean
        while (beanContext.getContextual() instanceof Decorator) {
            beanContext = getParentCreationalContext(beanContext);
        }
        Contextual<?> decoratedContextual = beanContext.getContextual();

        if (decoratedContextual instanceof Bean<?>) {
            Bean<?> bean = (Bean<?>) decoratedContextual;
            if (bean instanceof Serializable) {
                return bean;
            } else {
                return SerializableForwardingBean.of(getBeanManager().getContextId(), bean);
            }
        } else {
            InterceptorLogger.LOG.unableToDetermineInterceptedBean(ip);
            return null;
        }
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return Collections.<Annotation> singleton(DecoratedLiteral.INSTANCE);
    }

    @Override
    public String toString() {
        return "Implicit Bean [jakarta.enterprise.inject.spi.Bean] with qualifiers [@Decorated]";
    }
}
